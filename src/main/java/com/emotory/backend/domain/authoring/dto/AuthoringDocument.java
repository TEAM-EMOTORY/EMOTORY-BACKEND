package com.emotory.backend.domain.authoring.dto;

import com.emotory.backend.domain.story.entity.Emotion;
import com.emotory.backend.domain.interactiveStory.entity.StoryNodeType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/** Draft fields may be incomplete; publication requirements belong to the graph validator. */
public record AuthoringDocument(
        @Size(max = 255) String title,
        @Size(max = 255) String description,
        Emotion emotion,
        @Min(0) @Max(1000) Integer maxDepth,
        @Min(1) @Max(1000) Integer maxNodes,
        @Size(max = 100) String rootNodeId,
        @NotNull @Size(max = 1000) List<@NotNull @Valid Node> nodes
) {
    public record Node(
            @NotBlank @Size(max = 100) String id,
            @Size(max = 255) String title,
            @Size(max = 20000) String content,
            Emotion emotion,
            StoryNodeType type,
            @NotNull @Size(max = 10) List<@NotNull @Valid Choice> choices,
            @Valid Ending ending
    ) {}
    public record Choice(
            @NotBlank @Size(max = 100) String id,
            @Size(max = 255) String text,
            @Size(max = 100) String nextNodeId
    ) {}
    public record Ending(
            @Size(max = 300) String summary,
            @Size(max = 255) String emotionLabel,
            @Size(max = 20000) String contentSummary,
            @Size(max = 20) List<@NotNull @Valid Advice> advices
    ) {}
    public record Advice(@Size(max = 255) String title, @Size(max = 300) String description) {}
}
