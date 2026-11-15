package com.emotory.backend.domain.sharedStory.controller.docs;

import com.emotory.backend.domain.sharedStory.dto.response.SharedStoryListResponse;
import com.emotory.backend.domain.sharedStory.dto.response.SharedStoryDetailResponse;
import com.emotory.backend.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "다른 사람의 스토리", description = "사용자가 공개한 완료 스토리 조회 API")
public interface SharedStoryControllerDocs {

    @Operation(
            summary = "다른 사람의 스토리 목록 조회",
            description = "공개된 완료 스토리를 플레이 생성 시각이 최신인 순서로 조회합니다. 대표 이미지는 결말 이미지입니다."
    )
    @ApiResponse(responseCode = "200", description = "공개 스토리 목록 조회 성공")
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 페이지 번호 또는 페이지 크기",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    SharedStoryListResponse getStoryList(
            @Parameter(description = "0부터 시작하는 페이지 번호. 기본값 0") int page,
            @Parameter(description = "페이지 크기. 1~100, 기본값 20") int size
    );

    @Operation(
            summary = "다른 사람의 스토리 상세 조회",
            description = "공개된 완료 플레이의 이름, 감정, 제목과 실제 진행 순서의 장면을 조회합니다. nodes 배열은 결말까지 포함하며 각 항목에 id, imgSrc, story를 반환합니다. 이미지는 재생성하지 않습니다."
    )
    @ApiResponse(responseCode = "200", description = "공개 스토리 상세 조회 성공")
    @ApiResponse(
            responseCode = "404",
            description = "비공개·미완료 또는 존재하지 않는 플레이 세션",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    SharedStoryDetailResponse getStoryDetail(
            @Parameter(description = "플레이 세션 ID") Long playSessionId
    );
}
