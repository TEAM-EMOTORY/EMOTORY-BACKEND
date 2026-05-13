package com.emotory.backend.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;

public record MemberPrivacyRequest (
    @NotNull(message = "개인정보 동의 여부는 필수입니다.")
    Boolean isPrivacyAgreed
){}
