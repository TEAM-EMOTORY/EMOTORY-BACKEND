package com.emotory.backend.domain.member.controller.docs;

import com.emotory.backend.domain.member.dto.request.MemberPrivacyRequest;
import com.emotory.backend.domain.member.dto.response.MemberNameResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "사용자")
public interface MemberControllerDocs {

    @Operation(summary = "사용자 이름 조회", description = "사용자 ID로 사용자 이름을 조회합니다.")
    MemberNameResponse getMemberName(Long memberId);

    @Operation(summary = "사용자 약관 동의", description = "사용자 ID로 개인정보 동의 여부를 수정합니다.")
    void updatePrivacy(Long memberId, MemberPrivacyRequest request);
}
