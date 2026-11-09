package com.emotory.backend.domain.image.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** 세션에서 사용할 얼굴 사진과 첫 장면 참고 이미지를 고정한다. */
@Entity
@Table(name = "session_image_context")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionImageContext {
    @Id
    @Column(name = "play_session_id")
    private Long playSessionId;
    @Column(nullable = false, length = 1024)
    private String sourceFaceKey;
    @Column(length = 1024)
    private String faceSnapshotKey;
    @Column(length = 1024)
    private String referenceImageKey;
    @Column(length = 36)
    private String generationToken;
    private Instant leaseUntil;

    public static SessionImageContext create(Long sessionId, String faceKey, String referenceKey) {
        var context = new SessionImageContext();
        context.playSessionId = sessionId;
        context.sourceFaceKey = faceKey;
        context.referenceImageKey = referenceKey;
        return context;
    }

    public void claim(String token, Instant until) {
        generationToken = token;
        leaseUntil = until;
    }
    public void saveFace(String key) { faceSnapshotKey = key; }
    public void saveReference(String key) { referenceImageKey = key; }
    public void finish() { generationToken = null; leaseUntil = null; }
}
