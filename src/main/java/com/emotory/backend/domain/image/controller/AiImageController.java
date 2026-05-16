package com.emotory.backend.domain.image.controller;

import com.emotory.backend.domain.image.dto.request.AiImageGenerateRequest;
import com.emotory.backend.domain.image.dto.response.AiImageGenerateResponse;
import com.emotory.backend.domain.image.service.AiImageGenerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class AiImageController {

    private final AiImageGenerateService aiImageGenerateService;

    @PostMapping("/generate")
    public AiImageGenerateResponse generateImage(
            @RequestBody AiImageGenerateRequest request
    ) {

        // 이미지 생성
        String imageUrl =
                aiImageGenerateService.generateImage(
                        request.faceImageKey(),
                        request.nodeId(),
                        request.playSessionId()
                );

        return new AiImageGenerateResponse(imageUrl);
    }
}
