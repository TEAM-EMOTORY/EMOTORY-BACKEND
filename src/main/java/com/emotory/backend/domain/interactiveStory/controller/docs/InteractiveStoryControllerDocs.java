package com.emotory.backend.domain.interactiveStory.controller.docs;

import com.emotory.backend.domain.interactiveStory.dto.request.InteractiveStoryCreateRequest;
import com.emotory.backend.domain.interactiveStory.dto.response.InteractiveStoryCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인터랙티브 스토리 저장")
public interface InteractiveStoryControllerDocs {

    @Operation(
            summary = "인터랙티브 스토리 생성 API",
            description = """

                    인터랙티브 스토리 JSON 데이터를 저장합니다.

                    - STORY 노드와 ENDING 노드를 분리 저장합니다.
                    - 선택지와 다음 노드 관계를 함께 저장합니다.
                    - JSON 내부 임시 노드 ID를 실제 DB StoryNode와 매핑합니다.

                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "인터랙티브 스토리 생성 성공",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = InteractiveStoryCreateResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 값",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            value = """

                                    {
                                      "status": 400,
                                      "message": "잘못된 입력입니다."
                                    }

                                    """
                    )
            )
    )
    InteractiveStoryCreateResponse create(

            @Valid
            @RequestBody
            InteractiveStoryCreateRequest request

    );

    @Operation(
            summary = "인터랙티브 스토리 삭제 API",
            description = """

                    인터랙티브 스토리를 삭제합니다.

                    - 플레이 세션이나 공개 기록이 있으면 삭제할 수 없습니다(409).
                    - Choice 데이터를 먼저 삭제합니다.
                    - 이후 StoryNode 데이터를 삭제합니다.
                    - 마지막으로 Story 데이터를 삭제합니다.

                    """
    )
    @ApiResponse(
            responseCode = "204",
            description = "인터랙티브 스토리 삭제 성공"
    )
    @ApiResponse(
            responseCode = "404",
            description = "스토리를 찾을 수 없음",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                            value = """

                                    {
                                      "status": 404,
                                      "message": "스토리를 찾을 수 없습니다."
                                    }

                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "플레이 세션 또는 공개 기록이 있어 삭제할 수 없음",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = """
                            {
                              "status": 409,
                              "message": "플레이 세션이 있거나 공개 기록이 있는 동화는 삭제할 수 없습니다."
                            }
                            """)
            )
    )
    ResponseEntity<Void> delete(

            @Parameter(description = "스토리 ID", example = "1")
            @PathVariable Long storyId

    );
    @Operation(summary = "동화 공개 중단", description = "기존 동화 또는 최신 공개본을 목록에서 숨기고 새 플레이를 차단합니다. 기존 세션과 기록은 유지합니다. 반복 요청도 204입니다.")
    @ApiResponse(responseCode = "204", description = "공개 중단 완료")
    @ApiResponse(responseCode = "404", description = "동화 없음")
    @ApiResponse(responseCode = "409", description = "이전 공개본은 변경할 수 없음")
    ResponseEntity<Void> unpublish(@Parameter(description = "동화 ID") @PathVariable Long storyId);

    @Operation(summary = "동화 제공 재개", description = "기존 동화 또는 최신 공개본의 제공을 재개합니다. 새 공개 버전은 생성하지 않으며 이전 버전의 재개는 거절합니다.")
    @ApiResponse(responseCode = "204", description = "제공 재개 완료")
    @ApiResponse(responseCode = "404", description = "동화 없음")
    @ApiResponse(responseCode = "409", description = "이전 공개본은 변경할 수 없음")
    ResponseEntity<Void> resume(@Parameter(description = "동화 ID") @PathVariable Long storyId);
}
