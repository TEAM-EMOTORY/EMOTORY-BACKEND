package com.emotory.backend.domain.choice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선택지 선택 요청")
public record ChoiceSelectRequest(

        @Schema(
                description = "선택지 ID",
                example = "1"
        )
        Long choiceId
) {
}