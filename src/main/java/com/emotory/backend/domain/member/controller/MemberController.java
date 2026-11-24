package com.emotory.backend.domain.member.controller;

import com.emotory.backend.domain.member.controller.docs.MemberControllerDocs;
import com.emotory.backend.domain.member.dto.request.MemberCreateRequest;
import com.emotory.backend.domain.member.dto.response.MemberNameResponse;
import com.emotory.backend.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;

    @Override
    @PostMapping
    public ResponseEntity<Void> createMember(@Valid @RequestBody MemberCreateRequest request) {
        Long memberId = memberService.createMember(request);
        return ResponseEntity.created(URI.create("/members/" + memberId)).build();
    }

    @Override
    @GetMapping("/{memberId}")
    public MemberNameResponse getMemberName(@PathVariable Long memberId) {
        return memberService.getMemberName(memberId);
    }
}
