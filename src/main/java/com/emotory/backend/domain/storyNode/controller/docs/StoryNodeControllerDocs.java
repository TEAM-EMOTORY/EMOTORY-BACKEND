package com.emotory.backend.domain.storyNode.controller.docs;

import com.emotory.backend.domain.storyNode.dto.response.StoryNodeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "스토리 노드")
public interface StoryNodeControllerDocs {

    @Operation(summary = "현재 페이지 조회", description = "스토리 노드 ID로 현재 페이지와 선택지를 조회합니다.")
    StoryNodeResponse getStoryNode(Long storyNodeId);
}
