package com.emotory.backend.domain.choice.entity;

import com.emotory.backend.domain.storyNode.StoryNode;
import jakarta.persistence.*;

@Entity
public class Choice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "choice_id")
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(name = "next_node_id", nullable = false)
    private Long nextNodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private StoryNode storyNode;
}
