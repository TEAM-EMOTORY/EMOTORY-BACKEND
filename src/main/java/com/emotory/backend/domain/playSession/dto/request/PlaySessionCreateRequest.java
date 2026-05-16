package com.emotory.backend.domain.playSession.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이 세션 생성 요청")
public record PlaySessionCreateRequest(

        @Schema(
                description = "사용자 ID",
                example = "1"
        )
        Long memberId,

        @Schema(
                description = "스토리 ID",
                example = "1"
        )
        Long storyId
) {
}