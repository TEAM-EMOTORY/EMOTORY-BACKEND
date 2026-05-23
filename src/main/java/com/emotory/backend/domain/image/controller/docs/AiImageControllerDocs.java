package com.emotory.backend.domain.image.controller.docs;

import com.emotory.backend.domain.image.dto.request.AiImageGenerateRequest;
import com.emotory.backend.domain.image.dto.response.AiImageGenerateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "이미지")
public interface AiImageControllerDocs {

    @Operation(summary = "AI 이미지 생성", description = "얼굴 이미지와 스토리 노드를 기반으로 AI 이미지를 생성합니다.")
    AiImageGenerateResponse generateImage(
            @RequestBody AiImageGenerateRequest request
    );
}
