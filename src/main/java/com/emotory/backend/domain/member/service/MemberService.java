package com.emotory.backend.domain.member.service;

import com.emotory.backend.domain.member.dto.request.MemberPrivacyRequest;
import com.emotory.backend.domain.member.dto.response.MemberNameResponse;
import com.emotory.backend.domain.member.entity.Member;
import com.emotory.backend.domain.member.exception.MemberNotFoundException;
import com.emotory.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;


    public MemberNameResponse getMemberName(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        return MemberNameResponse.from(member);
    }

    @Transactional
    public void updatePrivacy(Long memberId, MemberPrivacyRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        LocalDateTime privacyAgreedAt = request.isPrivacyAgreed() ? LocalDateTime.now() : null;

        member.updatePrivacyAgreement(request.isPrivacyAgreed(), privacyAgreedAt);
    }
}
