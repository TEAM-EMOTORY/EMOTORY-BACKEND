package com.emotory.backend.domain.interactiveStory.dto.request;

import com.emotory.backend.domain.story.entity.Emotion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InteractiveStoryCreateRequest(
        @NotNull Emotion emotion,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull Integer maxDepth,
        @Valid @NotNull StoryFlowRequest story
) {
}