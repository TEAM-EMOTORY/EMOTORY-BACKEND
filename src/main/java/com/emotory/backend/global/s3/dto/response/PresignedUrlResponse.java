package com.emotory.backend.global.s3.dto.response;

public record PresignedUrlResponse (
        String uploadUrl,
        String fileKey
) {
}
