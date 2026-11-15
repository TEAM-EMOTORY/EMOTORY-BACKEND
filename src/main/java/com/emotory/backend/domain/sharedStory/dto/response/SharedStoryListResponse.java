package com.emotory.backend.domain.sharedStory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "공개 스토리 목록 응답")
public record SharedStoryListResponse(

        @Schema(description = "공개 스토리 목록")
        List<SharedStoryListItemResponse> stories,

        @Schema(description = "현재 페이지 번호. 0부터 시작", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {
}
