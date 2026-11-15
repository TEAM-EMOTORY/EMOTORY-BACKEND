package com.emotory.backend.domain.sharedStory.dto.response;

import com.emotory.backend.domain.story.entity.Emotion;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공개 스토리 목록 항목")
public record SharedStoryListItemResponse(

        @Schema(description = "플레이 세션 ID", example = "1")
        Long playSessionId,

        @Schema(description = "사용자 이름", example = "장정훈")
        String name,

        @Schema(description = "선택한 감정", example = "HAPPY")
        Emotion emotion,

        @Schema(description = "스토리 제목")
        String title,

        @Schema(description = "대표 이미지로 사용하는 결말 이미지 URL")
        String imageUrl
) {
}
