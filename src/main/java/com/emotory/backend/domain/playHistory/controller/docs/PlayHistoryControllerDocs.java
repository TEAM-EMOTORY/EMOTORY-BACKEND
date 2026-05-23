package com.emotory.backend.domain.playHistory.controller.docs;

import com.emotory.backend.domain.playHistory.dto.response.PlayHistoryListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "플레이 히스토리")
public interface PlayHistoryControllerDocs {

    @Operation(summary = "플레이 히스토리 목록 조회", description = "플레이 세션 ID로 선택 진행 히스토리를 조회합니다.")
    PlayHistoryListResponse getPlayHistories(
            @Parameter(description = "플레이 세션 ID")
            @PathVariable Long playSessionId
    );
}
