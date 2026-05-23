package com.emotory.backend.domain.interactiveStory.dto.request;

import com.emotory.backend.domain.interactiveStory.entity.StoryNodeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "스토리 노드 요청")
public record StoryNodeRequest(

        @Schema(description = "노드 임시 ID", example = "1-1")
        @NotBlank(message = "노드 ID는 필수입니다.")
        String id,

        @Schema(description = "노드 타입", example = "STORY")
        @NotNull(message = "노드 타입은 필수입니다.")
        StoryNodeType type,

        @Schema(description = "노드 깊이", example = "2")
        @NotNull(message = "노드 깊이는 필수입니다.")
        Integer depth,

        @Schema(description = "스토리 내용", example = "친구들과 함께 웃으며 즐거운 시간을 보냈어요.")
        String content,

        @ArraySchema(schema = @Schema(implementation = StoryChoiceRequest.class))
        @Valid
        List<StoryChoiceRequest> choices,

        @Schema(description = "엔딩 데이터")
        StoryEndingRequest ending
) {
}