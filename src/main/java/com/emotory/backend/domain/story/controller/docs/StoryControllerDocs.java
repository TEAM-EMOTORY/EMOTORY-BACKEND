package com.emotory.backend.domain.story.controller.docs;

import com.emotory.backend.domain.story.dto.response.StoryListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "이야기")
public interface StoryControllerDocs {

    @Operation(summary = "이야기 목록 조회", description = "제공 중인 기존 동화와 각 초안의 최신 공개 동화를 조회합니다. 이전 공개본과 공개 중단 동화는 제외합니다.")
    StoryListResponse getStories();
}
