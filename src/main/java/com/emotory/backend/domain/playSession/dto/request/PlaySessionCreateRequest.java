package com.emotory.backend.domain.playSession.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사용자·동화 기준 플레이 이어하기 또는 새 세션 생성 요청")
public record PlaySessionCreateRequest(

        @NotNull(message = "memberId는 필수입니다.")
        @Schema(
                description = "사용자 ID",
                example = "1"
        )
        Long memberId,

        @NotNull(message = "storyId는 필수입니다.")
        @Schema(
                description = "스토리 ID",
                example = "1"
        )
        Long storyId
) {
}
