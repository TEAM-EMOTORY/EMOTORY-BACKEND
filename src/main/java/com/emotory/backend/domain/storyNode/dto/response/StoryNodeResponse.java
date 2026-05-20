package com.emotory.backend.domain.storyNode.dto.response;

import java.util.List;

public record StoryNodeResponse(
        Long nodeId,
        String content,
        Integer nodeOrder,
        List<ChoiceResponse> choices
) {

    public record ChoiceResponse(
            Long choiceId,
            String content
    ){
    }
}
