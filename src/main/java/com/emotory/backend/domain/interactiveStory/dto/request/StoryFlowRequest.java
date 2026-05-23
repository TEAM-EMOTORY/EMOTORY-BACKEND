package com.emotory.backend.domain.interactiveStory.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "스토리 흐름 요청")
public record StoryFlowRequest(

        @Schema(description = "루트 노드 ID", example = "1")
        @NotBlank(message = "루트 노드 ID는 필수입니다.")
        String rootNodeId,

        @ArraySchema(schema = @Schema(implementation = StoryNodeRequest.class))
        @Valid
        @NotEmpty(message = "노드 목록은 비어있을 수 없습니다.")
        List<StoryNodeRequest> nodes
) {
}