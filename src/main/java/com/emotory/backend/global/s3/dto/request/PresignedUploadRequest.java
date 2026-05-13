package com.emotory.backend.global.s3.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PresignedUploadRequest(
        @NotBlank String type,
        @NotBlank String contentType
) {
}