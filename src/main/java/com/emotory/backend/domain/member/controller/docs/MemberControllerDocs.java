package com.emotory.backend.domain.member.controller.docs;

import com.emotory.backend.domain.member.dto.request.MemberCreateRequest;
import com.emotory.backend.domain.member.dto.response.MemberNameResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;

@Tag(name = "사용자")
public interface MemberControllerDocs {

    @Operation(summary = "사용자 생성", description = "사용자 이름과 업로드한 얼굴 사진을 저장합니다. 서버가 사진의 SHA-256과 고정 원본을 보관합니다. 응답 본문은 없으며 Location 헤더에서 생성된 사용자 ID를 확인합니다.")
    @ApiResponse(responseCode = "201", description = "사용자 생성 성공", headers = @Header(name = "Location", description = "생성된 사용자 경로", schema = @Schema(type = "string", example = "/members/123")))
    ResponseEntity<Void> createMember(MemberCreateRequest request);

    @Operation(summary = "사용자 이름 조회", description = "사용자 ID로 사용자 이름을 조회합니다.")
    MemberNameResponse getMemberName(Long memberId);
}
