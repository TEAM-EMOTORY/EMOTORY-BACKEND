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
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private Long age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(name = "face_image_url", nullable = false)
    private String faceImageUrl;

    @Column(name = "is_privacy_agreed", nullable = false)
    private Boolean isPrivacyAgreed;

    @Column(name = "privacy_agreed_at", nullable = false)
    private LocalDateTime privacyAgreedAt;

    private Member(
            String name,
            String faceImageUrl,
            MemberRole role,
            Boolean isPrivacyAgreed,
            LocalDateTime privacyAgreedAt
    ) {
        this.name = name;
        this.faceImageUrl = faceImageUrl;
        this.role = role;
        this.isPrivacyAgreed = isPrivacyAgreed;
        this.privacyAgreedAt = privacyAgreedAt;
    }

    public static Member create(String name, String faceImageUrl) {
        return new Member(name, faceImageUrl, MemberRole.USER, true, LocalDateTime.now());
    }
}
