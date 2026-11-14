package com.emotory.backend.domain.playSession.controller.docs;

import com.emotory.backend.domain.playSession.dto.request.PlaySessionCreateRequest;
import com.emotory.backend.domain.playSession.dto.request.PlaySessionEndRequest;
import com.emotory.backend.domain.playSession.dto.response.PlaySessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(
        name = "플레이 세션",
        description = "스토리 플레이 세션 API"
)
public interface PlaySessionControllerDocs {

    @Operation(
            summary = "플레이 세션 생성",
            description = "기존 동화 또는 최신 공개본으로 세션을 생성합니다. 이전 공개본의 새 플레이는 409로 거절하며, 기존 세션은 계속 사용할 수 있습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이 세션 생성 성공"
    )
    @ApiResponse(responseCode = "409", description = "이전 공개본 또는 공개 중단 동화이므로 시작 불가")
    PlaySessionResponse create(
            @RequestBody
            PlaySessionCreateRequest request
    );
    @Operation(
            summary = "플레이 세션 조회",
            description = "현재 진행 중인 플레이 세션 정보를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이 세션 조회 성공"
    )
    PlaySessionResponse get(
            @Parameter(description = "플레이 세션 ID")
            @PathVariable
            Long playSessionId
    );
    @Operation(
            summary = "플레이 종료 및 공개 여부 저장",
            description = "결말에 도달한 플레이를 종료하며 shared=true이면 모든 진행 장면의 이미지가 준비된 경우 함께 공개합니다. "
                    + "shared=false이면 비공개로 종료합니다. 같은 공개 여부로 재요청할 수 있지만 종료 후 공개 여부는 변경할 수 없습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이 종료 성공"
    )
    @ApiResponse(responseCode = "409", description = "결말 미도달, 공개 이미지 누락 또는 종료 후 공개 여부 변경")
    void end(
            @Parameter(description = "플레이 세션 ID")
            @PathVariable
            Long playSessionId,
            @RequestBody PlaySessionEndRequest request
    );
}
