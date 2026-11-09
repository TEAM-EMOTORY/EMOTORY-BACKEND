package com.emotory.backend.domain.story.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Story {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Emotion emotion;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    public void changeAvailability(boolean available) {
        this.available = available;
    }

    public static Story create(
            String title,
            String description,
            Emotion emotion
    ) {
        Story story = new Story();
        story.title = title;
        story.description = description;
        story.emotion = emotion;
        return story;
    }
}
