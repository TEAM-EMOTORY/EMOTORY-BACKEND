package com.emotory.backend.domain.member.entity;

import com.emotory.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long age;

    @Column(nullable = false)
    private String role;

    @Column(name = "face_image_url", nullable = false)
    private String faceImageUrl;

    @Column(name = "is_privacy_agreed", nullable = false)
    private Boolean isPrivacyAgreed;

    @Column(name = "privacy_agreed_at", nullable = false)
    private LocalDateTime privacyAgreedAt;
}
