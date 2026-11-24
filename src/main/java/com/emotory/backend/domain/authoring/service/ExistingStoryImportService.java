package com.emotory.backend.domain.authoring.service;

import com.emotory.backend.domain.authoring.dto.AuthoringDocument;
import com.emotory.backend.domain.authoring.dto.AuthoringDocument.*;
import com.emotory.backend.domain.authoring.dto.AuthoringDtos.*;
import com.emotory.backend.domain.authoring.entity.StoryDraft;
import com.emotory.backend.domain.authoring.entity.StoryPublication;
import com.emotory.backend.domain.authoring.repository.StoryDraftRepository;
import com.emotory.backend.domain.authoring.repository.StoryPublicationRepository;
import com.emotory.backend.domain.choice.repository.ChoiceRepository;
import com.emotory.backend.domain.interactiveStory.entity.StoryNodeType;
import com.emotory.backend.domain.story.repository.StoryRepository;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import com.emotory.backend.domain.storyNode.repository.StoryNodeRepository;
import com.emotory.backend.global.exception.ErrorCode;
import com.emotory.backend.global.exception.authoring.AuthoringException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExistingStoryImportService {
    private final StoryRepository stories;
    private final StoryNodeRepository nodes;
    private final ChoiceRepository choices;
    private final StoryDraftRepository drafts;
    private final StoryPublicationRepository publications;
    private final AuthoringValidator validator;
    private final jakarta.validation.Validator constraints;
    private final ObjectMapper mapper;
    private final AuthoringService authoring;

    public Page<ImportableStory> list(int page) {
        return stories.findImportableStories(PageRequest.of(Math.max(0, page), 20,
                        Sort.by("id").descending()))
                .map(s -> new ImportableStory(s.getId(), s.getTitle(), s.isAvailable()));
    }

    @Transactional
    public DraftResponse importStory(Long storyId) {
        // 첫 조회부터 동화를 잠근 뒤 공개 이력을 확인한다. 동시 가져오기도 같은 초안을 반환한다.
        var story = stories.findLockedById(storyId)
                .orElseThrow(() -> new AuthoringException(ErrorCode.STORY_NOT_FOUND));
        var existing = publications.findByStoryId(storyId);
        if (existing.isPresent()) return authoring.get(existing.get().getDraftId());

        var sourceNodes = nodes.findAllByStory(story);
        if (sourceNodes.isEmpty()) throw invalid(null, "nodes", "가져올 장면이 없습니다.");
        sourceNodes.sort(Comparator.comparing(StoryNode::getNodeOrder).thenComparing(StoryNode::getId));
        // 현재 플레이 시작 로직과 같은 시작점을 사용한다.
        var root = nodes.findFirstByStoryOrderByNodeOrderAsc(story).orElseThrow();
        var outgoing = choices.findByStoryNodeInOrderByIdAsc(sourceNodes).stream()
                .collect(Collectors.groupingBy(c -> c.getStoryNode().getId()));
        List<Node> importedNodes = new ArrayList<>();
        for (var node : sourceNodes) {
            var importedChoices = outgoing.getOrDefault(node.getId(), List.of()).stream()
                    .map(c -> new AuthoringDocument.Choice("choice-" + c.getId(), c.getContent(),
                            nodeId(c.getNextNode().getId())))
                    .toList();
            boolean ending = Boolean.TRUE.equals(node.getIsEnding());
            importedNodes.add(new Node(nodeId(node.getId()), "장면 " + node.getNodeOrder(),
                    ending ? "" : node.getContent(), node.getEmotion(),
                    ending ? StoryNodeType.ENDING : StoryNodeType.STORY, importedChoices,
                    ending ? parseEnding(node) : null));
        }
        var document = new AuthoringDocument(story.getTitle(), story.getDescription(), story.getEmotion(),
                1000, sourceNodes.size(), nodeId(root.getId()), importedNodes);
        var fieldErrors = constraints.validate(document).stream()
                .map(v -> new Issue("INVALID_FIELD", null, null, v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        if (!fieldErrors.isEmpty()) throw new AuthoringException(ErrorCode.AUTHORING_IMPORT_FAILED, fieldErrors);
        var validation = validator.validate(document, 0);
        if (!validation.valid()) throw new AuthoringException(ErrorCode.AUTHORING_IMPORT_FAILED, validation.errors());
        document = new AuthoringDocument(document.title(), document.description(), document.emotion(),
                validation.actualMaxDepth(), document.maxNodes(), document.rootNodeId(), document.nodes());

        var draft = drafts.save(StoryDraft.create(mapper.writeValueAsString(document)));
        // 원본을 첫 공개 버전으로 연결한다. 원본 장면/선택/공개 상태/플레이 데이터는 변경하지 않는다.
        draft.published(storyId);
        publications.save(StoryPublication.of(draft));
        return authoring.get(draft.getId());
    }

    private Ending parseEnding(StoryNode node) {
        String id = nodeId(node.getId());
        String content = node.getContent();
        if (content == null || content.isBlank()) throw invalid(id, "ending", "결말 내용이 없습니다.");
        String[] lines = content.split("\\R", -1);
        String summary = lines[0].trim();
        if (summary.isEmpty() || summary.startsWith("느낀 감정:") || summary.startsWith("조언:")
                || summary.startsWith("내용 요약:")) {
            throw invalid(id, "ending.summary", "결말 첫 줄에 요약이 필요합니다.");
        }
        String emotion = null;
        String scene = null;
        List<Advice> advice = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("느낀 감정:") && emotion == null) {
                emotion = line.substring("느낀 감정:".length()).trim();
            } else if (line.startsWith("내용 요약:") && scene == null) {
                scene = line.substring("내용 요약:".length()).trim();
            } else if (line.startsWith("조언:")) {
                String[] parts = line.substring("조언:".length()).split("\\|", 2);
                if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                    throw invalid(id, "ending.advices", "조언은 '조언: 제목 | 설명' 형식이어야 합니다.");
                }
                advice.add(new Advice(parts[0].trim(), parts[1].trim()));
            } else {
                throw invalid(id, "ending", "해석할 수 없는 결말 항목이 있습니다. 결말 형식을 확인하세요.");
            }
        }
        if (emotion == null || emotion.isBlank()) throw invalid(id, "ending.emotionLabel", "'느낀 감정:' 항목이 필요합니다.");
        if (advice.isEmpty()) throw invalid(id, "ending.advices", "조언이 하나 이상 필요합니다.");
        // 과거 결말은 내용 요약이 없을 수 있다. 기존 이미지 프롬프트와 동일하게 첫 줄을 사용한다.
        if (scene == null) scene = summary;
        if (scene.isBlank()) throw invalid(id, "ending.contentSummary", "내용 요약이 비어 있습니다.");
        return new Ending(summary, emotion, scene, List.copyOf(advice));
    }

    private String nodeId(Long id) { return "node-" + id; }

    private AuthoringException invalid(String nodeId, String field, String message) {
        return new AuthoringException(ErrorCode.AUTHORING_IMPORT_FAILED,
                List.of(new Issue("IMPORT_FORMAT", nodeId, null, field, message)));
    }
}
