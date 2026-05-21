package com.emotory.backend.domain.playHistory.dto.response;

import com.emotory.backend.domain.playHistory.entity.PlayHistory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "플레이 기록 응답")
public record PlayHistoryResponse(

        @Schema(description = "플레이 기록 ID", example = "1")
        Long playHistoryId,

        @Schema(description = "몇 번째 선택인지", example = "3")
        Integer stepNumber,

        @Schema(description = "선택이 발생한 노드 ID", example = "10")
        Long nodeId,

        @Schema(description = "선택한 선택지 ID", example = "25")
        Long choiceId,

        @Schema(description = "선택한 선택지 내용", example = "동굴 안으로 들어간다")
        String choiceContent,

        @Schema(description = "기록 생성 시각")
        LocalDateTime createdAt
) {

    public static PlayHistoryResponse from(PlayHistory playHistory) {
        return new PlayHistoryResponse(
                playHistory.getPlayHistoryId(),
                playHistory.getStepNumber(),
                playHistory.getStoryNode().getId(),
                playHistory.getChoice().getId(),
                playHistory.getChoice().getContent(),
                playHistory.getCreatedAt()
        );
    }
}
