package com.emotory.backend.domain.member.controller;

import com.emotory.backend.domain.member.controller.docs.MemberControllerDocs;
import com.emotory.backend.domain.member.dto.request.MemberCreateRequest;
import com.emotory.backend.domain.member.dto.response.MemberNameResponse;
import com.emotory.backend.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createMember(@Valid @RequestBody MemberCreateRequest request) {
        memberService.createMember(request);
    }

    @Override
    @GetMapping("/{memberId}")
    public MemberNameResponse getMemberName(@PathVariable Long memberId) {
        return memberService.getMemberName(memberId);
    }
}
