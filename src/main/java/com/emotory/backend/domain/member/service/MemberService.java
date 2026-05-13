package com.emotory.backend.domain.member.service;

import com.emotory.backend.domain.member.dto.request.MemberPrivacyRequest;
import com.emotory.backend.domain.member.entity.Member;
import com.emotory.backend.domain.member.repository.MemberRepository;
import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void updatePrivacy(Long memberId, MemberPrivacyRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        LocalDateTime privacyAgreedAt = request.isPrivacyAgreed() ? LocalDateTime.now() : null;

        member.updatePrivacyAgreement(request.isPrivacyAgreed(), privacyAgreedAt);
    }
}
