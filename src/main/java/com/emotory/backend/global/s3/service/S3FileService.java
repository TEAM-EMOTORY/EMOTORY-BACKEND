package com.emotory.backend.global.s3.service;

import com.emotory.backend.global.exception.s3.UnsupportedFileFormat;
import com.emotory.backend.global.exception.s3.UnsupportedImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3FileService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    public String createKey(String type, String contentType) {

        String extension = extractExtension(contentType);

        String uuid = UUID.randomUUID().toString();

        return switch (type) {
            case "face" ->
                    "faces/" + uuid + "." + extension;
            case "generated" ->
                    "generated/" + uuid + "." + extension;
            default ->
                    throw new UnsupportedImageType();
        };
    }

    /**
     * generated/ 경로의 합성 결과 이미지만 CloudFront 조회 URL로 변환
     */
    public String createGeneratedImageUrl(String key) {

        if (key == null || !key.startsWith("generated/")) {
            throw new UnsupportedImageType();
        }

        return "https://" + cloudFrontDomain + "/" + key;
    }

    private String extractExtension(String contentType) {

        return switch (contentType) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            default ->
                    throw new UnsupportedFileFormat();
        };
    }
}