package com.emotory.backend.domain.storyNode.entity;

import com.emotory.backend.domain.story.entity.Emotion;
import com.emotory.backend.domain.story.entity.Story;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class StoryNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "node_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(name = "node_order", nullable = false)
    private Integer nodeOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "node_is_ending", nullable = false)
    private Boolean isEnding;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_emotion")
    private Emotion emotion;
}
