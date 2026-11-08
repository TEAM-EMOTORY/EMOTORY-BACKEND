package com.emotory.backend.domain.authoring.service;

import com.emotory.backend.domain.authoring.dto.AuthoringDocument;
import com.emotory.backend.domain.authoring.dto.AuthoringDtos.*;
import com.emotory.backend.domain.authoring.entity.StoryDraft;
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
