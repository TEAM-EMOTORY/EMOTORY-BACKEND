package com.emotory.backend.domain.choice.controller.docs;

import com.emotory.backend.domain.choice.dto.request.ChoiceSelectRequest;
import com.emotory.backend.domain.choice.dto.response.ChoiceSelectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
        name = "선택지",
        description = "스토리 선택지 진행 API"
)
public interface ChoiceControllerDocs {

    @Operation(
            summary = "선택지 선택",
            description = "사용자의 선택에 따라 다음 스토리 노드로 진행합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "스토리 진행 성공"
    )
    ChoiceSelectResponse selectChoice(
            @Parameter(description = "플레이 세션 ID")
            @PathVariable
            Long playSessionId,
            @RequestBody
            ChoiceSelectRequest request
    );
}