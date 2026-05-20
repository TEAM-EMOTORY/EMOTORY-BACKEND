package com.emotory.backend.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사용자 생성 요청")
public record MemberCreateRequest(

        @Schema(description = "사용자 이름", example = "장정훈")
        @NotBlank(message = "사용자 이름은 필수입니다.")
        String name,

        @Schema(description = "사용자 얼굴 이미지 URL", example = "https://example.com/face.png")
        @NotBlank(message = "사용자 얼굴 이미지 URL은 필수입니다.")
        String faceImageUrl,

        @Schema(description = "개인정보 동의 여부", example = "true")
        @NotNull(message = "개인정보 동의 여부는 필수입니다.")
        @AssertTrue(message = "개인정보 약관 동의는 필수입니다.")
        Boolean isPrivacyAgreed
) {
}
