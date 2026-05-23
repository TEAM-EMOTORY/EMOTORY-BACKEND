package com.emotory.backend.domain.interactiveStory.dto.request;

import com.emotory.backend.domain.interactiveStory.entity.StoryNodeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StoryNodeRequest(
        @NotBlank String id,
        @NotNull StoryNodeType type,
        @NotNull Integer depth,
        String content,
        @Valid List<StoryChoiceRequest> choices,
        StoryEndingRequest ending
) {
}
