package com.emotory.backend.domain.story.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "이야기 목록 조회 응답")
public record StoryListResponse(

        @Schema(description = "이야기 목록")
        List<StoryResponse> stories
) {

    public static StoryListResponse from(List<StoryResponse> stories) {
        return new StoryListResponse(stories);
    }
}
