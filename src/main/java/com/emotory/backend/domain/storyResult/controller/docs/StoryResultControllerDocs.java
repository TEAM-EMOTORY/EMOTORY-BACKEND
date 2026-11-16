package com.emotory.backend.domain.storyResult.controller.docs;

import com.emotory.backend.domain.storyResult.dto.request.StoryResultCreateRequest;
import com.emotory.backend.domain.storyResult.dto.response.StoryResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "스토리 결과")
public interface StoryResultControllerDocs {

    @Operation(summary = "스토리 결과 생성", description = "결말에 도달한 세션의 결과가 없을 때만 저장합니다. 선택 시 자동 생성된 결과가 있으면 변경 없이 성공 응답을 반환합니다.")
    @ApiResponse(responseCode = "409", description = "결말에 도달하지 않은 세션")
    void createStoryResult(
            @Parameter(description = "플레이 세션 ID")
            @PathVariable Long playSessionId,
            @RequestBody StoryResultCreateRequest request
    );

    @Operation(summary = "스토리 결과 조회", description = "플레이 세션 ID로 스토리 결과를 조회합니다.")
    StoryResultResponse getStoryResult(
            @Parameter(description = "플레이 세션 ID")
            @PathVariable Long playSessionId
    );
}
