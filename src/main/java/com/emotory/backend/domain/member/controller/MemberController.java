package com.emotory.backend.domain.member.controller;

import com.emotory.backend.domain.member.dto.request.MemberPrivacyRequest;
import com.emotory.backend.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "사용자 약관 동의", description = "사용자 ID로 개인정보 동의 여부를 수정합니다.")
    @PatchMapping("/{memberId}/privacy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePrivacy(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberPrivacyRequest request
    ) {
        memberService.updatePrivacy(memberId, request);
    }
}