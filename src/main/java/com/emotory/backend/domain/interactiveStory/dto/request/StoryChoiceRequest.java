package com.emotory.backend.domain.interactiveStory.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StoryChoiceRequest(
        @NotBlank String id,
        @NotBlank String text,
        @NotBlank String nextNodeId
) {
}