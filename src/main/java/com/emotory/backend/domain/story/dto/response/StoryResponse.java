package com.emotory.backend.domain.story.dto.response;

import com.emotory.backend.domain.story.entity.Story;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스토리 목록 조회 응답")
public record StoryResponse(

        @Schema(description = "스토리 ID", example = "1")
        Long storyId,

        @Schema(description = "스토리 제목", example = "정훈이의 감정여행~")
        String title,

        @Schema(description = "스토리 설명", example = "숨속을 탐험하며 감정을 알아가는 이야기")
        String description
) {

    public static StoryResponse from(Story story) {
        return new StoryResponse(story.getId(), story.getTitle(), story.getDescription());
    }
}
