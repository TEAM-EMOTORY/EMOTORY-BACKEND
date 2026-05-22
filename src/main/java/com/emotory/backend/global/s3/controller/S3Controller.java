package com.emotory.backend.global.s3.controller;

import com.emotory.backend.global.s3.controller.docs.S3ControllerDocs;
import com.emotory.backend.global.s3.dto.request.PresignedUploadRequest;
import com.emotory.backend.global.s3.dto.response.PresignedUrlResponse;
import com.emotory.backend.global.s3.service.PresignedS3Service;
import com.emotory.backend.global.s3.service.S3FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URL;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller implements S3ControllerDocs {

    private final PresignedS3Service presignedS3Service;
    private final S3FileService s3FileService;

    @Override
    @PostMapping("/presigned-upload")
    public PresignedUrlResponse getPreSignedUploadUrl(
            @Valid @RequestBody PresignedUploadRequest request
    ) {
        String key = s3FileService.createKey(request.type(), request.contentType());
        URL uploadUrl = presignedS3Service.generatePreSignedUploadUrl(key, request.contentType());

        return new PresignedUrlResponse(uploadUrl.toString(), key);
    }
}
