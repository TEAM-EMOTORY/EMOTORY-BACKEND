package com.emotory.backend.domain.member.dto.response;

import com.emotory.backend.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 이름 조회 응답")
public record MemberNameResponse (

        @Schema(description = "사용자 이름", example = "장정훈")
        String name
) {

    public static MemberNameResponse from(Member member) {
        return new MemberNameResponse(member.getName());
    }
}
