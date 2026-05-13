package com.emotory.backend.domain.member.service;

import com.emotory.backend.domain.member.dto.response.MemberNameResponse;
import com.emotory.backend.domain.member.entity.Member;
import com.emotory.backend.domain.member.repository.MemberRepository;
import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberNameResponse getMemberName(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return MemberNameResponse.from(member);
    }
}
