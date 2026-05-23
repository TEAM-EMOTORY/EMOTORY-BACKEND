package com.emotory.backend.domain.interactiveStory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "스토리 엔딩 요청")
public record StoryEndingRequest(

        @Schema(description = "사용자가 선택한 선택지", example = "친구들과 기쁨을 나눴어요")
        @NotBlank(message = "선택 선택지는 필수입니다.")
        String selectedChoiceText,

        @Schema(description = "스토리 요약", example = "행복한 발표회였어요.")
        @NotBlank(message = "요약은 필수입니다.")
        String summary,

        @Schema(description = "감정 라벨", example = "기쁨 😊")
        @NotBlank(message = "감정 라벨은 필수입니다.")
        String emotionLabel,

        @Schema(description = "엔딩 내용 요약", example = "친구들과 함께 행복한 시간을 보냈어요.")
        @NotBlank(message = "내용 요약은 필수입니다.")
        String contentSummary,

        @Schema(description = "감정 조언", example = "행복한 순간은 함께 나누면 더 커져요.")
        @NotBlank(message = "조언은 필수입니다.")
        String advice
) {
}