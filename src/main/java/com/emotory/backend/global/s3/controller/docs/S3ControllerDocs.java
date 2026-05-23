package com.emotory.backend.global.s3.controller.docs;

import com.emotory.backend.global.s3.dto.request.PresignedUploadRequest;
import com.emotory.backend.global.s3.dto.response.PresignedUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "파일 업로드")
public interface S3ControllerDocs {

    @Operation(summary = "Presigned Upload URL 발급", description = "S3 파일 업로드를 위한 Presigned URL과 객체 key를 발급합니다.")
    PresignedUrlResponse getPreSignedUploadUrl(
            @RequestBody PresignedUploadRequest request
    );
}
