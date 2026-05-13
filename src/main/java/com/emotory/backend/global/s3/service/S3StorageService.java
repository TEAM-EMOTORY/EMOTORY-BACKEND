package com.emotory.backend.global.s3.service;

import com.emotory.backend.global.exception.s3.S3DownloadFailedException;
import com.emotory.backend.global.exception.s3.S3FileNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * byte[] 기반 업로드
     * 이미지 전체가 이미 메모리에 올라와 있는 경우 사용
     */
    public void uploadFile(
            String key,
            byte[] fileBytes,
            String contentType
    ) {

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(fileBytes)
        );
    }

    /**
     * 얼굴사진의 보호를 위한 삭제 메서드
     */
    public void deleteFile(String key) {

        if (key == null || key.isBlank()) {
            return;
        }

        DeleteObjectRequest deleteRequest =
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build();

        s3Client.deleteObject(deleteRequest);
    }

    /**
     * 이미지 합성을 위한 S3에서 사용자 사진 다운받는 메서드
     */
    public byte[] downloadFile(String key) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> inputStream =
                     s3Client.getObject(request)) {
            return inputStream.readAllBytes();

        } catch (NoSuchKeyException e) {
            throw new S3FileNotFoundException();

        } catch (IOException | S3Exception e) {
            throw new S3DownloadFailedException();
        }
    }
}
