package com.emotory.backend.domain.authoring.service;

import com.emotory.backend.domain.authoring.dto.AuthoringDocument;
import com.emotory.backend.domain.authoring.dto.AuthoringDtos.*;
import com.emotory.backend.domain.authoring.entity.StoryDraft;
import com.emotory.backend.domain.authoring.entity.StoryPublication;
import com.emotory.backend.domain.authoring.repository.StoryPublicationRepository;
import com.emotory.backend.domain.story.repository.StoryRepository;
import com.emotory.backend.domain.authoring.repository.StoryDraftRepository;
import com.emotory.backend.global.exception.ErrorCode;
import com.emotory.backend.global.exception.authoring.AuthoringException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthoringService {
    private final StoryDraftRepository drafts;
    private final AuthoringValidator validator;
    private final ObjectMapper mapper;
    private final StoryPublicationRepository publications;
    private final StoryRepository stories;
    private final AuthoringPublicationMapper publicationMapper;
    private final jakarta.validation.Validator constraints;

    @Transactional
    public DraftResponse create(CreateRequest request) {
        var document = new AuthoringDocument(request == null ? null : request.title(), null, null,
                null, null, null, List.of());
        return response(drafts.save(StoryDraft.create(mapper.writeValueAsString(document))));
    }

    public DraftResponse get(Long id) {
        return response(find(id));
    }

    @Transactional
    public DraftResponse save(Long id, SaveRequest request) {
        StoryDraft draft = drafts.findLockedById(id)
                .orElseThrow(() -> new AuthoringException(ErrorCode.AUTHORING_DRAFT_NOT_FOUND));
        checkRevision(draft, request.revision());
        validator.checkDraftIdentity(request.document());
        draft.saveDocument(mapper.writeValueAsString(request.document()));
        return response(draft);
    }

    public ValidationResponse validate(Long id, RevisionRequest request) {
        StoryDraft draft = find(id);
        checkRevision(draft, request.revision());
        return validator.validate(document(draft), draft.getRevision());
    }

    private StoryDraft find(Long id) {
        return drafts.findById(id)
                .orElseThrow(() -> new AuthoringException(ErrorCode.AUTHORING_DRAFT_NOT_FOUND));
    }

    @Transactional
    public PublishResponse publish(Long id, RevisionRequest request) {
        // 저장·공개·중복 공개 요청이 같은 초안 잠금을 사용해 순서대로 처리되도록 한다.
        StoryDraft draft = drafts.findLockedById(id)
                .orElseThrow(() -> new AuthoringException(ErrorCode.AUTHORING_DRAFT_NOT_FOUND));
        checkRevision(draft, request.revision());
        if (Objects.equals(draft.getPublishedRevision(), draft.getRevision())) {
            // 중단된 동일 revision은 새 버전을 만들지 않고 기존 공개본의 제공을 재개한다.
            var story = stories.findLockedById(draft.getPublishedStoryId())
                    .orElseThrow(() -> new AuthoringException(ErrorCode.AUTHORING_PUBLICATION_DELETED));
            story.changeAvailability(true);
            return publishedResponse(draft, true);
        }
        AuthoringDocument document = document(draft);
        var fieldErrors = constraints.validate(document).stream()
                .map(v -> new Issue("INVALID_FIELD", null, null, v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        if (!fieldErrors.isEmpty()) throw new AuthoringException(ErrorCode.AUTHORING_VALIDATION_FAILED, fieldErrors);
        ValidationResponse validation = validator.validate(document, draft.getRevision());
        if (!validation.valid()) {
            throw new AuthoringException(ErrorCode.AUTHORING_VALIDATION_FAILED, validation.errors());
        }
        var story = publicationMapper.createStory(document);
        draft.published(story.getId());
        publications.save(StoryPublication.of(draft));
        return publishedResponse(draft, false);
    }

    private PublishResponse publishedResponse(StoryDraft draft, boolean reused) {
        return new PublishResponse(draft.getId(), draft.getRevision(), draft.getPublishedStoryId(),
                draft.getPublishedVersion(), reused);
    }

    private void checkRevision(StoryDraft draft, Long revision) {
        if (!Objects.equals(draft.getRevision(), revision)) {
            throw new AuthoringException(ErrorCode.AUTHORING_REVISION_CONFLICT);
        }
    }

    private AuthoringDocument document(StoryDraft draft) {
        return mapper.readValue(draft.getDocumentJson(), AuthoringDocument.class);
    }

    private DraftResponse response(StoryDraft draft) {
        return new DraftResponse(draft.getId(), draft.getRevision(), draft.getUpdatedAt(),
                draft.getPublishedStoryId(), draft.getPublishedRevision(), document(draft));
    }
}
