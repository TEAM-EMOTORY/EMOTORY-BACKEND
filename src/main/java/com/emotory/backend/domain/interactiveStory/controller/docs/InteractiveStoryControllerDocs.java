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
    ResponseEntity<Void> delete(

            @Parameter(description = "스토리 ID", example = "1")
            @PathVariable Long storyId

    );
}