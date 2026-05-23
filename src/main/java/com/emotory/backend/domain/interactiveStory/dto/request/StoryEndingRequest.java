package com.emotory.backend.domain.interactiveStory.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StoryEndingRequest(
        @NotBlank String selectedChoiceText,
        @NotBlank String summary,
        @NotBlank String emotionLabel,
        @NotBlank String contentSummary,
        @NotBlank String advice
) {
}