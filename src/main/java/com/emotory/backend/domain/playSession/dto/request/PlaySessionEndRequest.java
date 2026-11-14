package com.emotory.backend.domain.playSession.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "플레이 종료 요청")
public record PlaySessionEndRequest(
        @NotNull(message = "공개 여부는 필수입니다.")
        @Schema(description = "완료한 스토리의 공개 여부", example = "true")
        Boolean shared
) {}
