package com.emotory.backend.domain.image.dto.request;

public record AiImageGenerateRequest(
        String faceImageKey,
        Long nodeId,
        Long playSessionId
) {
}