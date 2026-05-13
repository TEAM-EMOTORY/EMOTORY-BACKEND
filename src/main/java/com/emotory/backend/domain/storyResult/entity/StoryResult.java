package com.emotory.backend.domain.storyResult.entity;

import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class StoryResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id", nullable = false)
    private Long id;

    @Lob
    @Column(nullable = false)
    private String summary;

    @Column(nullable = false)
    private String emotion;

    @Lob
    @Column(nullable = false)
    private String advice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "play_session_id", nullable = false)
    private PlaySession playSession;
}
