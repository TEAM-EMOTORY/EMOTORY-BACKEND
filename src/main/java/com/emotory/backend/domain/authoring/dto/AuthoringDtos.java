package com.emotory.backend.domain.authoring.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class AuthoringDtos {
    private AuthoringDtos() {}
    public record CreateRequest(@Size(max = 255) String title) {}
    public record SaveRequest(@NotNull @PositiveOrZero Long revision,
                              @NotNull @Valid AuthoringDocument document) {}
    public record RevisionRequest(@NotNull @PositiveOrZero Long revision) {}
    public record DraftResponse(Long id, long revision, Instant updatedAt,
                                Long publishedStoryId, Long publishedRevision, AuthoringDocument document) {}
    public record Issue(String code, String nodeId, String choiceId, String field, String message) {}
    public record ValidationResponse(long revision, boolean valid, List<Issue> errors,
                                     Integer actualMaxDepth, Map<String, Integer> depths) {}
}
