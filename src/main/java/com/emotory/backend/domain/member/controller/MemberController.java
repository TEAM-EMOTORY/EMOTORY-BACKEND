package com.emotory.backend.domain.member.controller;

import com.emotory.backend.domain.member.controller.docs.MemberControllerDocs;
import com.emotory.backend.domain.member.dto.request.MemberPrivacyRequest;
import com.emotory.backend.domain.member.dto.response.MemberNameResponse;
import com.emotory.backend.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @Override
    @PatchMapping("/{memberId}/privacy")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePrivacy(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberPrivacyRequest request
    ) {
        memberService.updatePrivacy(memberId, request);
    }
}
