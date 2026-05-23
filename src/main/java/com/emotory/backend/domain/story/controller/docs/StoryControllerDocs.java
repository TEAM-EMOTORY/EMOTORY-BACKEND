package com.emotory.backend.domain.story.controller.docs;

import com.emotory.backend.domain.story.dto.response.StoryListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "이야기")
public interface StoryControllerDocs {

    @Operation(summary = "이야기 목록 조회", description = "사용자가 선택할 수 있는 이야기 목록을 조회합니다.")
    StoryListResponse getStories();
}
