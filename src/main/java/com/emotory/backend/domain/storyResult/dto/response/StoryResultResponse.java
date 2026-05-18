package com.emotory.backend.domain.storyResult.dto.response;

public record StoryResultResponse(
        String summary,
        String emotion,
        String advice,
        String generatedImageUrl
) {
}
