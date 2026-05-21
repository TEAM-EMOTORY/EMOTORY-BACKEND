package com.emotory.backend.domain.storyResult.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoryResultCreateRequest(

        @NotBlank
        String emotion,

        @NotBlank
        @Size(max = 300)
        String summary,

        @NotBlank
        @Size(max = 300)
        String advice
) {
}
