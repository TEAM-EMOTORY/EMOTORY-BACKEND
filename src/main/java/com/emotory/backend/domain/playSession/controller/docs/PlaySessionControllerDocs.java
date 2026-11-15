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
            summary = "진행 중인 플레이 이어하기 또는 새 세션 생성",
            description = "같은 memberId·storyId의 PLAYING 세션 중 결말에 도달하지 않은 세션을 반환합니다. "
                    + "여러 개면 최근 수정 시각(없으면 생성 시각), 세션 ID 내림차순으로 선택합니다. "
                    + "기존 세션이 없을 때만 새 세션을 생성하며, 새 플레이는 최신 공개본·공개 중인 동화만 가능합니다. "
                    + "기존 세션은 이전 공개본 또는 공개 중단 상태여도 이어할 수 있습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "기존 플레이 세션 반환 또는 새 세션 생성 성공"
    )
    @ApiResponse(responseCode = "409", description = "이어갈 세션이 없고 이전 공개본 또는 공개 중단 동화이므로 새 플레이 시작 불가")
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
