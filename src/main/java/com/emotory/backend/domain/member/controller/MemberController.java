package com.emotory.backend.domain.member.controller;

import com.emotory.backend.domain.member.controller.docs.MemberControllerDocs;
import com.emotory.backend.domain.member.dto.response.MemberNameResponse;
import com.emotory.backend.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;

    @Override
    @GetMapping("/{memberId}")
    public MemberNameResponse getMemberName(@PathVariable Long memberId) {
        return memberService.getMemberName(memberId);
    }
}
