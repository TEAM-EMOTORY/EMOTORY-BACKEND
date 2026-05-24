package com.emotory.backend.domain.interactiveStory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인터랙티브 스토리 생성 응답")
public record InteractiveStoryCreateResponse(

        @Schema(description = "생성된 스토리 ID", example = "1")
        Long storyId
) {
}