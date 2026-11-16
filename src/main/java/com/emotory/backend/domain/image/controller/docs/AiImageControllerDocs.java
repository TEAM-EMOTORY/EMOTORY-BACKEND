package com.emotory.backend.domain.image.controller.docs;

import com.emotory.backend.domain.image.dto.request.AiImageGenerateRequest;
import com.emotory.backend.domain.image.dto.response.AiImageGenerateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "이미지")
public interface AiImageControllerDocs {

    @Operation(summary = "AI 이미지 생성", description = "시작 장면을 먼저 요청합니다. 같은 이름·사진·동화의 기존 참고 이미지를 연결한 플레이는 장면별 생성 이미지를 재사용합니다. 기존 이미지가 없는 장면은 고정된 얼굴과 시작 장면 이미지를 참고해 생성합니다.")
    @ApiResponse(responseCode = "200", description = "생성 또는 기존 이미지 URL 반환")
    @ApiResponse(responseCode = "400", description = "잘못된 세션·동화 연결, 얼굴 이미지 경로 또는 형식")
    @ApiResponse(responseCode = "409", description = "얼굴 교체, 시작 장면 미생성, 동일 세션 생성 중 또는 작업 만료")
    AiImageGenerateResponse generateImage(
            @RequestBody AiImageGenerateRequest request
    );
}
