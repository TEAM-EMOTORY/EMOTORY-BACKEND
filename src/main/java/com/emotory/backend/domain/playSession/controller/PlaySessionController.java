package com.emotory.backend.domain.playSession.controller;

import com.emotory.backend.domain.playSession.controller.docs.PlaySessionControllerDocs;
import com.emotory.backend.domain.playSession.dto.request.PlaySessionCreateRequest;
import com.emotory.backend.domain.playSession.dto.request.PlaySessionEndRequest;
import com.emotory.backend.domain.playSession.dto.response.PlaySessionResponse;
import com.emotory.backend.domain.playSession.service.PlaySessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/play-sessions")
public class PlaySessionController implements PlaySessionControllerDocs {

    private final PlaySessionService playSessionService;

    // 매번 처음부터 플레이하며 같은 이름·사진의 기존 장면 이미지만 재사용한다.
    @Override
    @PostMapping
    public PlaySessionResponse create(
            @Valid
            @RequestBody
            PlaySessionCreateRequest request
    ) {

        return playSessionService.create(
                request
        );
    }

    // 플레이 세션 조회
    @Override
    @GetMapping("/{playSessionId}")
    public PlaySessionResponse get(
            @PathVariable Long playSessionId
    ) {

        return playSessionService.get(
                playSessionId
        );
    }

    // 플레이 종료
    @Override
    @PatchMapping("/{playSessionId}/end")
    public void end(
            @PathVariable Long playSessionId,
            @Valid @RequestBody PlaySessionEndRequest request
    ) {

        playSessionService.end(
                playSessionId,
                request
        );
    }
}
