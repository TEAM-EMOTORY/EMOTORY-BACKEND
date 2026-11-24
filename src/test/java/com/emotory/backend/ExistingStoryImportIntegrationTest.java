package com.emotory.backend;

import com.emotory.backend.domain.authoring.dto.AuthoringDocument;
import com.emotory.backend.domain.authoring.dto.AuthoringDtos.*;
import com.emotory.backend.domain.authoring.repository.StoryDraftRepository;
import com.emotory.backend.domain.authoring.repository.StoryPublicationRepository;
import com.emotory.backend.domain.authoring.service.AuthoringService;
import com.emotory.backend.domain.authoring.service.ExistingStoryImportService;
import com.emotory.backend.domain.choice.entity.Choice;
import com.emotory.backend.domain.generatedImage.entity.GeneratedImage;
import com.emotory.backend.domain.member.entity.Member;
import com.emotory.backend.domain.playHistory.entity.PlayHistory;
import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.playSession.service.PlaySessionService;
import com.emotory.backend.domain.story.entity.Emotion;
import com.emotory.backend.domain.story.entity.Story;
import com.emotory.backend.domain.story.repository.StoryRepository;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import com.emotory.backend.global.exception.ErrorCode;
import com.emotory.backend.global.exception.authoring.AuthoringException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(properties = "spring.config.location=classpath:/application-test.properties")
@AutoConfigureMockMvc
@Transactional
class ExistingStoryImportIntegrationTest {
    @Autowired ExistingStoryImportService imports;
    @Autowired AuthoringService authoring;
    @Autowired StoryDraftRepository drafts;
    @Autowired StoryPublicationRepository publications;
    @Autowired StoryRepository stories;
    @Autowired PlaySessionService sessions;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired WebApplicationContext context;
    MockMvc mvc;

    private static final String ENDING = "친구가 되었어요.\n느낀 감정: 기쁨\n내용 요약: 함께 놀아요.\n조언: 인사 | 먼저 인사해요.\n조언: 경청 | 이야기를 들어요.";
    private record Source(long storyId, long rootId, long endingId, long choiceId) {}

    @BeforeEach
    void setupMvc() {
        mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private Source source(String title, String endingText, boolean available) {
        var story = Story.create(title, "기존 동화 설명", Emotion.HAPPY);
        story.changeAvailability(available);
        em.persist(story);
        var root = StoryNode.create(story, "숲에서 친구를 만났어요.", 1, false, Emotion.HAPPY);
        var end = StoryNode.create(story, endingText, 2, true, Emotion.SAD);
        em.persist(root);
        em.persist(end);
        var choice = Choice.create(root, "인사하기", end);
        em.persist(choice);
        em.flush();
        return new Source(story.getId(), root.getId(), end.getId(), choice.getId());
    }

    @Test
    void importsGraphAndEndingAsLinkedFirstPublicationWithoutDuplicatingOriginal() {
        var src = source("가져올 동화", ENDING, true);
        long storyCount = stories.count();
        var draft = imports.importStory(src.storyId());
        assertThat(draft.publishedStoryId()).isEqualTo(src.storyId());
        assertThat(draft.publishedRevision()).isZero();
        assertThat(draft.document().rootNodeId()).isEqualTo("node-" + src.rootId());
        assertThat(draft.document().maxDepth()).isEqualTo(1);
        assertThat(draft.document().maxNodes()).isEqualTo(2);
        assertThat(draft.document().nodes().get(0).choices().get(0).nextNodeId()).isEqualTo("node-" + src.endingId());
        var ending = draft.document().nodes().get(1);
        assertThat(ending.emotion()).isEqualTo(Emotion.SAD);
        assertThat(ending.ending().contentSummary()).isEqualTo("함께 놀아요.");
        assertThat(ending.ending().advices()).hasSize(2);
        assertThat(stories.count()).isEqualTo(storyCount);
        assertThat(stories.findAvailableStories()).extracting(Story::getId).contains(src.storyId());
        assertThat(imports.list(0).getContent()).extracting(ImportableStory::storyId).doesNotContain(src.storyId());
        assertThat(publications.findByStoryId(src.storyId()).orElseThrow().getVersion()).isEqualTo(1);
        var same = authoring.publish(draft.id(), new RevisionRequest(0L));
        assertThat(same.reused()).isTrue();
        assertThat(same.storyId()).isEqualTo(src.storyId());
    }

    @Test
    void reimportReturnsExistingDraftWithoutOverwritingEditsOrCreatingPublications() {
        var src = source("중복 가져오기", ENDING, true);
        var draft = imports.importStory(src.storyId());
        var edited = renamed(draft.document(), "편집 중인 제목");
        authoring.save(draft.id(), new SaveRequest(0L, edited));
        long count = publications.count();
        var repeated = imports.importStory(src.storyId());
        assertThat(repeated.id()).isEqualTo(draft.id());
        assertThat(repeated.revision()).isEqualTo(1);
        assertThat(repeated.document().title()).isEqualTo("편집 중인 제목");
        assertThat(publications.count()).isEqualTo(count);
    }

    @Test
    void keepsUnpublishedStoryStoppedAndSupportsOldEndingWithoutSceneSummary() {
        var src = source("중단된 동화", "요약\r\n느낀 감정: 기쁨\r\n조언: 인사 | 인사해요.\r\n", false);
        var draft = imports.importStory(src.storyId());
        assertThat(stories.findById(src.storyId()).orElseThrow().isAvailable()).isFalse();
        assertThat(stories.findAvailableStories()).extracting(Story::getId).doesNotContain(src.storyId());
        assertThat(draft.document().nodes().get(1).ending().contentSummary()).isEqualTo("요약");
    }

    @Test
    void editedPublicationCreatesNewVersionAndPreservesExistingPlayHistoryAndImages() {
        var src = source("수정 전 동화", ENDING, true);
        var member = Member.create("기존 사용자", "https://test.invalid/faces/test.png", null, null);
        em.persist(member);
        var session = new PlaySession(member, em.find(Story.class, src.storyId()), em.find(StoryNode.class, src.rootId()));
        em.persist(session);
        var history = PlayHistory.of(1, session, em.find(StoryNode.class, src.rootId()), em.find(Choice.class, src.choiceId()));
        em.persist(history);
        var image = new GeneratedImage("https://test.invalid/generated/old.png", session, em.find(StoryNode.class, src.rootId()));
        em.persist(image);
        em.flush();
        var draft = imports.importStory(src.storyId());
        authoring.save(draft.id(), new SaveRequest(0L, renamed(draft.document(), "수정 후 동화")));
        var published = authoring.publish(draft.id(), new RevisionRequest(1L));
        assertThat(published.version()).isEqualTo(2);
        assertThat(published.storyId()).isNotEqualTo(src.storyId());
        assertThat(stories.findAvailableStories()).extracting(Story::getId)
                .contains(published.storyId()).doesNotContain(src.storyId());
        em.flush(); em.clear();
        assertThat(em.find(PlaySession.class, session.getId()).getStory().getId()).isEqualTo(src.storyId());
        assertThat(em.find(PlayHistory.class, history.getPlayHistoryId()).getChoice().getId()).isEqualTo(src.choiceId());
        assertThat(em.find(GeneratedImage.class, image.getGeneratedImageId()).getImageUrl()).endsWith("/old.png");
        assertThat(sessions.get(session.getId()).playSessionId())
                .isEqualTo(session.getId());
        assertThat(imports.importStory(src.storyId()).id()).isEqualTo(draft.id());
    }

    @Test
    void rejectsMalformedEndingWithoutWritingPartialDraftOrChangingStory() {
        var src = source("잘못된 결말", "요약\n느낀 감정: 기쁨\n조언: 구분자 없는 조언", false);
        long count = drafts.count();
        assertThatThrownBy(() -> imports.importStory(src.storyId()))
                .isInstanceOfSatisfying(AuthoringException.class, e -> {
                    assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTHORING_IMPORT_FAILED);
                    assertThat(e.getErrors().get(0).nodeId()).isEqualTo("node-" + src.endingId());
                    assertThat(e.getErrors().get(0).field()).isEqualTo("ending.advices");
                });
        assertThat(drafts.count()).isEqualTo(count);
        assertThat(publications.findByStoryId(src.storyId())).isEmpty();
        assertThat(stories.findById(src.storyId()).orElseThrow().isAvailable()).isFalse();
    }

    @Test
    void rejectsCrossStoryConnectionAndMissingStory() {
        var src = source("잘못된 연결", ENDING, true);
        var other = source("다른 동화", ENDING, true);
        em.persist(Choice.create(em.find(StoryNode.class, src.rootId()), "다른 동화로", em.find(StoryNode.class, other.endingId())));
        assertThatThrownBy(() -> imports.importStory(src.storyId()))
                .isInstanceOfSatisfying(AuthoringException.class, e ->
                        assertThat(e.getErrors()).extracting(Issue::code).contains("TARGET_NOT_FOUND"));
        assertThatThrownBy(() -> imports.importStory(Long.MAX_VALUE))
                .isInstanceOfSatisfying(AuthoringException.class, e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.STORY_NOT_FOUND));
    }

    @Test
    void adminPageShowsImportButtonRedirectsToEditorAndEscapesSourceTitle() throws Exception {
        var src = source("<script>既存</script>", ENDING, true);
        mvc.perform(get("/admin/stories").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("초안으로 가져오기")))
                .andExpect(content().string(containsString("&lt;script&gt;")));
        var draft = mvc.perform(post("/admin/stories/import/{id}", src.storyId())
                        .with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection()).andReturn();
        mvc.perform(get(draft.getResponse().getRedirectedUrl()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(view().name("admin/editor"));
    }

    @Test
    void importRequiresAdminAndCsrfAndReturnsDetailedFormatErrors() throws Exception {
        var src = source("불완전 결말", "요약\n느낀 감정: 기쁨\n알 수 없는 줄", true);
        mvc.perform(post("/authoring/stories/import/{id}", src.storyId()).with(csrf()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/authoring/stories/import/{id}", src.storyId()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/authoring/stories/import/{id}", src.storyId()).with(user("user").roles("USER")).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/authoring/stories/importable").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].storyId").value(src.storyId()));
        mvc.perform(post("/authoring/stories/import/{id}", src.storyId()).with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].nodeId").value("node-" + src.endingId()));
        mvc.perform(post("/admin/stories/import/{id}", src.storyId()).with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isBadRequest()).andExpect(view().name("admin/error"))
                .andExpect(content().string(containsString("해석할 수 없는 결말 항목")));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentImportsCreateOnlyOneDraftAndPublication() throws Exception {
        var tx = new TransactionTemplate(transactionManager);
        var src = tx.execute(status -> source("동시 가져오기", ENDING, true));
        var pool = Executors.newFixedThreadPool(2);
        var barrier = new CyclicBarrier(2);
        try {
            Callable<Long> task = () -> { barrier.await(5, TimeUnit.SECONDS); return imports.importStory(src.storyId()).id(); };
            var first = pool.submit(task);
            var second = pool.submit(task);
            assertThat(first.get(15, TimeUnit.SECONDS)).isEqualTo(second.get(15, TimeUnit.SECONDS));
            tx.executeWithoutResult(status -> assertThat(em.createQuery(
                    "select count(p) from StoryPublication p where p.storyId = :id", Long.class)
                    .setParameter("id", src.storyId()).getSingleResult()).isEqualTo(1L));
        } finally {
            pool.shutdownNow();
            pool.awaitTermination(5, TimeUnit.SECONDS);
            tx.executeWithoutResult(status -> {
                publications.findByStoryId(src.storyId()).ifPresent(p -> {
                    long draftId = p.getDraftId();
                    publications.delete(p); publications.flush(); drafts.deleteById(draftId);
                });
                em.createQuery("delete Choice c where c.storyNode.story.id = :id").setParameter("id", src.storyId()).executeUpdate();
                em.createQuery("delete StoryNode n where n.story.id = :id").setParameter("id", src.storyId()).executeUpdate();
                stories.deleteById(src.storyId());
            });
        }
    }

    private AuthoringDocument renamed(AuthoringDocument source, String title) {
        return new AuthoringDocument(title, source.description(), source.emotion(), source.maxDepth(),
                source.maxNodes(), source.rootNodeId(), source.nodes());
    }
}
