package com.emotory.backend.domain.authoring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "story_draft")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryDraft {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob @Column(name = "document_json", nullable = false, columnDefinition = "LONGTEXT")
    private String documentJson;

    @Column(nullable = false)
    private long revision;

    @Column(nullable = false)
    private Instant updatedAt;

    private Long publishedStoryId;
    private Long publishedRevision;

    @Column(nullable = false)
    private int publishedVersion;

    public static StoryDraft create(String json) {
        StoryDraft draft = new StoryDraft();
        draft.documentJson = json;
        draft.updatedAt = Instant.now();
        return draft;
    }
    public void saveDocument(String json) {
        documentJson = json;
        revision++;
        updatedAt = Instant.now();
    }
    public void published(Long storyId) {
        publishedStoryId = storyId;
        publishedRevision = revision;
        publishedVersion++;
    }
}
