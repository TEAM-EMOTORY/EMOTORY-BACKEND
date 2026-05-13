package com.emotory.backend.domain.generatedImage.entity;

import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import com.emotory.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class GeneratedImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "generated_image_id", nullable = false)
    private Long generatedImageId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "play_session_id", nullable = false)
    private PlaySession playSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private StoryNode storyNode;
}
