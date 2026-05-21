package com.emotory.backend.domain.playHistory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "플레이 기록 목록 응답")
public record PlayHistoryListResponse(

        @Schema(description = "플레이 기록 목록")
        List<PlayHistoryResponse> histories
) {

    public static PlayHistoryListResponse from(List<PlayHistoryResponse> histories) {
        return new PlayHistoryListResponse(histories);
    }
}
