package com.emotory.backend.domain.playSession.dto.response;

import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.playSession.entity.PlaySessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이 세션 응답")

public record PlaySessionResponse(

        @Schema(
                description = "플레이 세션 ID",
                example = "1"
        )
        Long playSessionId,

        @Schema(
                description = "현재 스토리 노드 ID",
                example = "3"
        )
        Long currentNodeId,

        @Schema(
                description = "플레이 상태",
                example = "PLAYING"
        )
        PlaySessionStatus status
) {

    public static PlaySessionResponse from(
            PlaySession playSession
    ) {

        return new PlaySessionResponse(
                playSession.getId(),
                playSession.getCurrentNode().getId(),
                playSession.getStatus()
        );
    }
}