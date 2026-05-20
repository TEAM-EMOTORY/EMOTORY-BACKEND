package com.emotory.backend.domain.playSession.entity;

import com.emotory.backend.domain.member.entity.Member;
import com.emotory.backend.domain.story.entity.Story;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import com.emotory.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PlaySession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "play_session_id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaySessionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_node_id")
    private StoryNode currentNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    public PlaySession(
            Member member,
            Story story,
            StoryNode currentNode
    ) {

        this.member = member;
        this.story = story;
        this.currentNode = currentNode;
        this.status = PlaySessionStatus.PLAYING;
    }

    public void changeCurrentNode(
            StoryNode node
    ) {

        this.currentNode = node;
    }

    public void end() {
        this.status = PlaySessionStatus.ENDED;
    }
}
