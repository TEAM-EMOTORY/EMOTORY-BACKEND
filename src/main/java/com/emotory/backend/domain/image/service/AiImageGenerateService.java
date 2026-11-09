package com.emotory.backend.domain.image.service;

import com.emotory.backend.domain.image.client.ExternalAiImageClient;
import com.emotory.backend.domain.image.client.ImageInput;
import com.emotory.backend.global.s3.service.S3FileService;
import com.emotory.backend.global.s3.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiImageGenerateService {
    private final S3StorageService s3StorageService;
    private final S3FileService s3FileService;
    private final ExternalAiImageClient externalAiImageClient;
    private final ImageGenerationStateService state;

    // 느린 외부 호출을 DB 트랜잭션과 분리해 다른 선택 요청을 오래 잠그지 않는다.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String generateImage(String faceImageKey, Long nodeId, Long playSessionId) {
        var claim = state.claim(playSessionId, nodeId, faceImageKey);
        if (claim.cachedUrl() != null) return claim.cachedUrl();
        try {
            byte[] faceImage;
            if (claim.faceSnapshotKey() == null) {
                faceImage = s3StorageService.downloadFile(claim.sourceFaceKey());
                ImageInput input = ImageInput.from(faceImage);
                String faceKey = "image-references/" + playSessionId + "/face-" + UUID.randomUUID() + "." + input.extension();
                s3StorageService.uploadFile(faceKey, faceImage, input.contentType());
                state.saveFace(claim, faceKey);
            } else {
                faceImage = s3StorageService.downloadFile(claim.faceSnapshotKey());
            }
            byte[] referenceImage = claim.referenceKey() == null ? null
                    : s3StorageService.downloadFile(claim.referenceKey());
            // 별도 캐릭터 생성 없이 장면당 기존과 동일하게 한 번만 생성한다.
            byte[] generatedImage = externalAiImageClient.generateImage(faceImage, referenceImage, claim.prompt());
            String generatedKey = s3FileService.createKey("generated", "image/png");
            s3StorageService.uploadFile(generatedKey, generatedImage, "image/png");
            String imageUrl = s3FileService.createGeneratedImageUrl(generatedKey);
            return state.complete(claim, generatedKey, imageUrl);
        } catch (RuntimeException exception) {
            try { state.fail(claim); }
            catch (RuntimeException stateFailure) { exception.addSuppressed(stateFailure); }
            throw exception;
        }
    }
}
