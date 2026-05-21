package com.emotory.backend.domain.playHistory.entity;

import com.emotory.backend.domain.choice.entity.Choice;
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
public class PlayHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "play_history_id", nullable = false)
    private Long playHistoryId;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "play_session_id", nullable = false)
    private PlaySession playSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private StoryNode storyNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "choice_id", nullable = false)
    private Choice choice;

    private PlayHistory(
            Integer stepNumber,
            PlaySession playSession,
            StoryNode storyNode,
            Choice choice
    ) {
        this.stepNumber = stepNumber;
        this.playSession = playSession;
        this.storyNode = storyNode;
        this.choice = choice;
    }

    public static PlayHistory of(
            Integer stepNumber,
            PlaySession playSession,
            StoryNode storyNode,
            Choice choice
    ) {
        return new PlayHistory(stepNumber, playSession, storyNode, choice);
    }
}
