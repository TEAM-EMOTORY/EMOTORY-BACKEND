package com.emotory.backend.domain.storyResult.dto.response;

import java.util.List;

public record StoryResultResponse(
        String summary,
        String emotion,
        List<AdviceResponse> advices,
        String generatedImageUrl
) {

    public record AdviceResponse(
            Long adviceId,
            String title,
            String description
    ) {
    }
}