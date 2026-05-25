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

    @Column(columnDefinition = "TEXT", nullable = false)
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

    public static StoryNode create(
            Story story,
            String content,
            Integer nodeOrder,
            Boolean isEnding,
            Emotion emotion
    ) {
        StoryNode storyNode = new StoryNode();
        storyNode.story = story;
        storyNode.content = content;
        storyNode.nodeOrder = nodeOrder;
        storyNode.isEnding = isEnding;
        storyNode.emotion = emotion;
        return storyNode;
    }
}
