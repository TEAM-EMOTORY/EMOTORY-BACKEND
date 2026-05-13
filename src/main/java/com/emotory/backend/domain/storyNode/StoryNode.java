package com.emotory.backend.domain.storyNode;

import com.emotory.backend.domain.story.entity.Story;
import jakarta.persistence.*;

@Entity
public class StoryNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "node_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(name="node_order", nullable = false)
    private Integer nodeOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;
}
