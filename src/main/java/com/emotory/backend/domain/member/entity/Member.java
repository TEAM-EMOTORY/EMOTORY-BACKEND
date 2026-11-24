package com.emotory.backend.domain.member.entity;

import com.emotory.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(indexes = @Index(name = "idx_member_name_face_sha256", columnList = "name, face_image_sha256"))
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(name = "face_image_url", nullable = false)
    private String faceImageUrl;

    @Column(name = "face_image_sha256", length = 64)
    private String faceImageSha256;

    @Column(name = "face_snapshot_key", length = 1024)
    private String faceSnapshotKey;

    @Column(name = "is_privacy_agreed", nullable = false)
    private Boolean isPrivacyAgreed;

    @Column(name = "privacy_agreed_at", nullable = false)
    private LocalDateTime privacyAgreedAt;

    private Member(
            String name,
            String faceImageUrl,
            MemberRole role,
            Boolean isPrivacyAgreed,
            LocalDateTime privacyAgreedAt,
            String faceImageSha256,
            String faceSnapshotKey
    ) {
        this.name = name;
        this.faceImageUrl = faceImageUrl;
        this.role = role;
        this.isPrivacyAgreed = isPrivacyAgreed;
        this.privacyAgreedAt = privacyAgreedAt;
        this.faceImageSha256 = faceImageSha256;
        this.faceSnapshotKey = faceSnapshotKey;
    }

    public static Member create(String name, String faceImageUrl, String faceImageSha256, String faceSnapshotKey) {
        return new Member(name, faceImageUrl, MemberRole.USER, true, LocalDateTime.now(), faceImageSha256, faceSnapshotKey);
    }
}
