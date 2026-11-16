package com.emotory.backend.global.s3.service;

import com.emotory.backend.global.exception.s3.UnsupportedFileFormat;
import com.emotory.backend.global.exception.s3.UnsupportedImageType;
import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
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

    /**
     * 얼굴 원본 이미지 조회 URL
     */
    public String createFaceImageUrl(String key) {

        if (key == null || !key.startsWith("faces/")) {
            throw new UnsupportedImageType();
        }

        return "https://" + cloudFrontDomain + "/" + key;
    }

    public String extractFaceImageKey(String imageUrl) {
        try {
            URI uri = URI.create(imageUrl);
            String path = uri.getPath();
            if ("https".equals(uri.getScheme()) && cloudFrontDomain.equalsIgnoreCase(uri.getHost())
                    && uri.getPort() == -1 && uri.getRawUserInfo() == null
                    && uri.getRawQuery() == null && uri.getRawFragment() == null
                    && path != null && path.startsWith("/faces/") && path.length() > 7 && path.length() <= 1025) {
                return path.substring(1);
            }
        } catch (IllegalArgumentException ignored) {
            // 업로드 버킷의 얼굴 사진만 읽으며 외부 URL은 다운로드하지 않는다.
        }
        throw new CustomException(ErrorCode.INVALID_IMAGE_REQUEST);
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
