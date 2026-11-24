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
            summary = "처음부터 새 플레이 시작",
            description = "매 요청마다 시작 장면에서 새 플레이를 생성합니다. memberId는 사용자 생성 응답의 Location 헤더에서 얻습니다. "
                    + "서버가 이름·얼굴 사진 SHA-256·동화 공개본이 같은 이전 플레이의 참고 이미지를 연결합니다. "
                    + "이미 생성한 장면은 이미지 생성 API에서 기존 URL을 재사용하고, 처음 방문하는 장면만 생성합니다. "
                    + "기존 플레이의 선택 기록과 공개 결과는 유지되며, 새 플레이는 최신 공개본·공개 중인 동화만 가능합니다. "
                    + "이름이나 사진이 다르거나 사진 해시가 없는 과거 데이터는 이미지를 자동 재사용하지 않습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "시작 장면에서 새 플레이 생성 성공"
    )
    @ApiResponse(responseCode = "400", description = "필수 값 누락 또는 잘못된 사용자 얼굴 사진 URL")
    @ApiResponse(responseCode = "409", description = "새 세션 생성 시 이전 공개본 또는 공개 중단 동화이므로 시작 불가")
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
