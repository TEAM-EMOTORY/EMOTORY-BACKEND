package com.emotory.backend.domain.interactiveStory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "스토리 선택지 요청")
public record StoryChoiceRequest(

        @Schema(description = "선택지 임시 ID", example = "1")
        @NotBlank(message = "선택지 ID는 필수입니다.")
        String id,

        @Schema(description = "선택지 내용", example = "친구들과 기쁨을 나눴어요")
        @NotBlank(message = "선택지 내용은 필수입니다.")
        String text,

        @Schema(description = "다음 노드 ID", example = "1-1")
        @NotBlank(message = "다음 노드 ID는 필수입니다.")
        String nextNodeId
) {
}