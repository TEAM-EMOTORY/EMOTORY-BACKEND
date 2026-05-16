package com.emotory.backend.domain.choice.dto.response;

import com.emotory.backend.domain.playSession.entity.PlaySession;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스토리 진행 응답")
public record ChoiceSelectResponse(
        @Schema(example = "1")
        Long playSessionId,

        @Schema(example = "3")
        Long currentNodeId,

        @Schema(example = "PLAYING")
        PlaySessionStatus status
) {

    public static ChoiceSelectResponse from(
            PlaySession playSession
    ) {

        return new ChoiceSelectResponse(
                playSession.getId(),
                playSession.getCurrentNode().getId(),
                playSession.getStatus()
        );
    }
}