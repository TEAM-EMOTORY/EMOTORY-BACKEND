package com.emotory.backend.domain.member.service;

import com.emotory.backend.domain.member.dto.request.MemberCreateRequest;
import com.emotory.backend.domain.member.dto.response.MemberNameResponse;
import com.emotory.backend.domain.member.entity.Member;
import com.emotory.backend.domain.member.exception.MemberNotFoundException;
import com.emotory.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void createMember(MemberCreateRequest request) {
        Member member = Member.create(request.name(), request.faceImageUrl());
        memberRepository.save(member);
    }

    public MemberNameResponse getMemberName(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        return MemberNameResponse.from(member);
    }
}
