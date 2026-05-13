package com.emotory.backend.domain.member.controller;

import com.emotory.backend.domain.member.dto.response.MemberNameResponse;
import com.emotory.backend.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "사용자 이름 조회", description = "사용자 ID로 사용자 이름을 조회합니다.")
    @GetMapping("/{memberId}")
    public MemberNameResponse getMemberName(@PathVariable Long memberId) {
        return memberService.getMemberName(memberId);
    }
}
