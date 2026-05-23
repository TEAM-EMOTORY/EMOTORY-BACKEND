package com.emotory.backend.domain.interactiveStory.dto.request;

import com.emotory.backend.domain.story.entity.Emotion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "인터랙티브 스토리 생성 요청")
public record InteractiveStoryCreateRequest(

        @Schema(description = "스토리 대표 감정", example = "HAPPY")
        @NotNull(message = "감정은 필수입니다.")
        Emotion emotion,

        @Schema(description = "스토리 제목", example = "처음으로 무대에서 노래했어요")
        @NotBlank(message = "스토리 제목은 필수입니다.")
        String title,

        @Schema(description = "스토리 설명", example = "발표회에서 노래를 부르며 기쁨과 자신감을 느끼는 이야기")
        @NotBlank(message = "스토리 설명은 필수입니다.")
        String description,

        @Schema(description = "스토리 최대 깊이", example = "3")
        @NotNull(message = "최대 깊이는 필수입니다.")
        Integer maxDepth,

        @Schema(description = "스토리 흐름 데이터")
        @Valid
        @NotNull(message = "스토리 데이터는 필수입니다.")
        StoryFlowRequest story
) {
}