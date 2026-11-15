package com.emotory.backend.domain.image.service;

import com.emotory.backend.domain.generatedImage.entity.GeneratedImage;
import com.emotory.backend.domain.generatedImage.repository.GeneratedImageRepository;
import com.emotory.backend.domain.image.entity.SessionImageContext;
import com.emotory.backend.domain.image.repository.SessionImageContextRepository;
import com.emotory.backend.domain.playSession.repository.PlaySessionRepository;
import com.emotory.backend.domain.storyNode.repository.StoryNodeRepository;
import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;
import com.emotory.backend.global.exception.playSession.PlaySessionNotFoundException;
import com.emotory.backend.global.exception.storyNode.StoryNodeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** 짧은 트랜잭션에서만 세션을 잠그고 S3·OpenAI 호출 중에는 잠금을 유지하지 않는다. */
@Service
@RequiredArgsConstructor
@Transactional
public class ImageGenerationStateService {
    private static final Duration LEASE = Duration.ofMinutes(15);
    private final PlaySessionRepository sessions;
    private final StoryNodeRepository nodes;
    private final GeneratedImageRepository generated;
    private final SessionImageContextRepository contexts;
    private final PromptTemplateService prompts;

    public record Claim(Long sessionId, Long nodeId, String token, String cachedUrl,
                        String sourceFaceKey, String faceSnapshotKey, String referenceKey, String prompt) {}

    public Claim claim(Long sessionId, Long nodeId, String suppliedFaceKey) {
        if (sessionId == null || nodeId == null) throw error(ErrorCode.INVALID_IMAGE_REQUEST);
        var session = sessions.findLockedById(sessionId).orElseThrow(PlaySessionNotFoundException::new);
        var node = nodes.findById(nodeId).orElseThrow(StoryNodeNotFoundException::new);
        if (!node.getStory().getId().equals(session.getStory().getId())) throw error(ErrorCode.INVALID_IMAGE_REQUEST);
        var context = contexts.findById(sessionId).orElse(null);
        if (context != null && suppliedFaceKey != null && !suppliedFaceKey.isBlank()
                && !context.getSourceFaceKey().equals(suppliedFaceKey)) throw error(ErrorCode.FACE_IMAGE_LOCKED);
        var cached = generated.findByPlaySessionAndStoryNode(session, node).orElse(null);
        if (cached != null && context == null) {
            // 기존 이미지는 재생성 없이 반환하고 다음 신규 생성에서 시작 장면의 참고 이미지를 연결한다.
            return new Claim(sessionId, nodeId, null, cached.getImageUrl(), null, null, null, null);
        }
        if (context == null) {
            if (suppliedFaceKey == null || !suppliedFaceKey.startsWith("faces/") || suppliedFaceKey.length() > 1024)
                throw error(ErrorCode.INVALID_IMAGE_REQUEST);
            var root = nodes.findFirstByStoryOrderByNodeOrderAsc(session.getStory())
                    .orElseThrow(StoryNodeNotFoundException::new);
            var oldRoot = generated.findByPlaySessionAndStoryNode(session, root).orElse(null);
            if (oldRoot == null && !root.getId().equals(nodeId)) throw error(ErrorCode.IMAGE_REFERENCE_REQUIRED);
            context = contexts.save(SessionImageContext.create(sessionId, suppliedFaceKey,
                    oldRoot == null ? null : generatedKey(oldRoot.getImageUrl())));
        }
        if (cached != null) return new Claim(sessionId, nodeId, null, cached.getImageUrl(), null, null, null, null);
        if (context.getReferenceImageKey() == null) {
            var root = nodes.findFirstByStoryOrderByNodeOrderAsc(session.getStory()).orElseThrow(StoryNodeNotFoundException::new);
            if (!root.getId().equals(nodeId)) throw error(ErrorCode.IMAGE_REFERENCE_REQUIRED);
        }
        if (context.getGenerationToken() != null && context.getLeaseUntil() != null
                && context.getLeaseUntil().isAfter(Instant.now())) throw error(ErrorCode.IMAGE_GENERATION_IN_PROGRESS);
        String token = UUID.randomUUID().toString();
        context.claim(token, Instant.now().plus(LEASE));
        return new Claim(sessionId, nodeId, token, null, context.getSourceFaceKey(), context.getFaceSnapshotKey(),
                context.getReferenceImageKey(), prompts.buildPrompt(node, context.getReferenceImageKey() != null));
    }

    public void saveFace(Claim claim, String key) {
        var context = owned(claim);
        context.saveFace(key);
        context.claim(claim.token(), Instant.now().plus(LEASE));
    }

    public String complete(Claim claim, String imageKey, String imageUrl) {
        var context = owned(claim);
        var session = sessions.findById(claim.sessionId()).orElseThrow(PlaySessionNotFoundException::new);
        var node = nodes.findById(claim.nodeId()).orElseThrow(StoryNodeNotFoundException::new);
        var cached = generated.findByPlaySessionAndStoryNode(session, node).orElse(null);
        if (cached == null) generated.save(new GeneratedImage(imageUrl, session, node));
        if (context.getReferenceImageKey() == null) context.saveReference(imageKey);
        context.finish();
        return cached == null ? imageUrl : cached.getImageUrl();
    }

    public void fail(Claim claim) {
        sessions.findLockedById(claim.sessionId()).orElseThrow(PlaySessionNotFoundException::new);
        contexts.findById(claim.sessionId()).ifPresent(context -> {
            // 기한이 지난 이전 작업이 새 작업의 생성 상태를 해제하지 못하게 한다.
            if (Objects.equals(context.getGenerationToken(), claim.token())) context.finish();
        });
    }

    private SessionImageContext owned(Claim claim) {
        sessions.findLockedById(claim.sessionId()).orElseThrow(PlaySessionNotFoundException::new);
        var context = contexts.findById(claim.sessionId()).orElseThrow(() -> error(ErrorCode.IMAGE_GENERATION_EXPIRED));
        if (!Objects.equals(context.getGenerationToken(), claim.token()) || context.getLeaseUntil() == null
                || !context.getLeaseUntil().isAfter(Instant.now())) throw error(ErrorCode.IMAGE_GENERATION_EXPIRED);
        return context;
    }

    private String generatedKey(String imageUrl) {
        try {
            String path = URI.create(imageUrl).getPath();
            if (path != null && path.startsWith("/generated/") && path.length() <= 1025) return path.substring(1);
        } catch (IllegalArgumentException ignored) {
            // 기존 URL을 현재 S3 키로 해석할 수 없으면 외부 URL을 임의로 다운로드하지 않는다.
        }
        throw error(ErrorCode.IMAGE_REFERENCE_UNAVAILABLE);
    }

    private CustomException error(ErrorCode code) { return new CustomException(code); }
}
