package com.emotory.backend.domain.authoring.controller;

import com.emotory.backend.domain.authoring.dto.AuthoringDtos.*;
import com.emotory.backend.domain.authoring.service.AuthoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authoring/stories")
@Tag(name = "동화 제작", description = "동화 초안 작성·저장 및 분기 구조 검사")
public class AuthoringController {
    private final AuthoringService authoringService;

    @PostMapping
    @Operation(summary = "동화 초안 생성", description = "빈 본문 또는 선택적인 title을 받아 revision 0의 빈 초안을 만듭니다.")
    public ResponseEntity<DraftResponse> create(@Valid @RequestBody(required = false) CreateRequest request) {
        var response = authoringService.create(request);
        return ResponseEntity.created(URI.create("/authoring/stories/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "초안 조회")
    public DraftResponse get(@PathVariable Long id) { return authoringService.get(id); }

    @PutMapping("/{id}")
    @Operation(summary = "초안 전체 저장", description = "빈 장면 본문과 미연결 선택지는 허용합니다. revision 충돌은 409입니다.")
    public DraftResponse save(@PathVariable Long id, @Valid @RequestBody SaveRequest request) { return authoringService.save(id, request); }

    @PostMapping("/{id}/validate")
    @Operation(summary = "저장된 초안 구조 검사", description = "구조 오류가 있어도 200으로 valid=false와 오류 목록을 반환합니다.")
    public ValidationResponse validate(@PathVariable Long id, @Valid @RequestBody RevisionRequest request) {
        return authoringService.validate(id, request);
    }

}
