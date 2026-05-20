package com.emotory.backend.domain.image.service;

import com.emotory.backend.domain.generatedImage.entity.GeneratedImage;
import com.emotory.backend.domain.generatedImage.repository.GeneratedImageRepository;
import com.emotory.backend.domain.image.client.ExternalAiImageClient;
import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.playSession.repository.PlaySessionRepository;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import com.emotory.backend.domain.storyNode.repository.StoryNodeRepository;
import com.emotory.backend.global.exception.playSession.PlaySessionNotFoundException;
import com.emotory.backend.global.exception.storyNode.StoryNodeNotFoundException;
import com.emotory.backend.global.s3.service.S3FileService;
import com.emotory.backend.global.s3.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiImageGenerateService {

    private final S3StorageService s3StorageService;
    private final S3FileService s3FileService;
    private final PromptTemplateService promptTemplateService;
    private final ExternalAiImageClient externalAiImageClient;
    private final GeneratedImageRepository generatedImageRepository;
    private final StoryNodeRepository storyNodeRepository;
    private final PlaySessionRepository playSessionRepository;

    public String generateImage(
            String faceImageKey,
            Long nodeId,
            Long playSessionId
    ) {
        StoryNode node =
                storyNodeRepository.findById(nodeId)
                        .orElseThrow(StoryNodeNotFoundException::new);

        PlaySession playSession =
                playSessionRepository.findById(playSessionId)
                        .orElseThrow(PlaySessionNotFoundException::new);

        // 이미 생성된 이미지 조회
        GeneratedImage existingImage =
                generatedImageRepository
                        .findByPlaySessionAndStoryNode(
                                playSession,
                                node
                        )
                        .orElse(null);

        // 이미 있으면 기존 URL 반환
        if (existingImage != null) {
            return existingImage.getImageUrl();
        }

        // 1. 프롬프트 생성
        String finalPrompt =
                promptTemplateService.buildPrompt(node);

        // 2. S3에서 얼굴 이미지 다운로드
        byte[] faceImage =
                s3StorageService.downloadFile(faceImageKey);

        // 3. OpenAI 이미지 생성
        byte[] generatedImage =
                externalAiImageClient.generateImage(
                        faceImage,
                        finalPrompt
                );

        String generatedKey =
                s3FileService.createKey(
                        "generated",
                        "image/png"
                );

        // 5. S3 업로드
        s3StorageService.uploadFile(
                generatedKey,
                generatedImage,
                "image/png"
        );

        // 6. CloudFront URL 생성
        String imageUrl =
                s3FileService.createGeneratedImageUrl(
                        generatedKey
                );

        // 7. DB 저장
        GeneratedImage image =
                new GeneratedImage(
                        imageUrl,
                        playSession,
                        node
                );
        generatedImageRepository.save(image);

        // 8. 프론트 반환
        return imageUrl;
    }
}