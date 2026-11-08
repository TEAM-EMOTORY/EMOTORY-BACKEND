package com.emotory.backend.domain.authoring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "story_publication", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"draft_id", "source_revision"}),
        @UniqueConstraint(columnNames = {"draft_id", "version_no"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryPublication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "draft_id", nullable = false)
    private Long draftId;
    @Column(name = "source_revision", nullable = false)
    private long sourceRevision;
    @Column(name = "story_id", nullable = false, unique = true)
    private Long storyId;
    @Column(name = "version_no", nullable = false)
    private int version;
    @Lob @Column(name = "document_json", nullable = false, columnDefinition = "LONGTEXT")
    private String documentJson;
    @Column(nullable = false)
    private Instant publishedAt;

    public static StoryPublication of(StoryDraft draft) {
        StoryPublication publication = new StoryPublication();
        publication.draftId = draft.getId();
        publication.sourceRevision = draft.getRevision();
        publication.storyId = draft.getPublishedStoryId();
        publication.version = draft.getPublishedVersion();
        publication.documentJson = draft.getDocumentJson();
        publication.publishedAt = Instant.now();
        return publication;
    }
}
