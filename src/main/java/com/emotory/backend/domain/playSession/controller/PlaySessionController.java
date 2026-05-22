package com.emotory.backend.domain.playSession.controller;

import com.emotory.backend.domain.playSession.controller.docs.PlaySessionControllerDocs;
import com.emotory.backend.domain.playSession.dto.request.PlaySessionCreateRequest;
import com.emotory.backend.domain.playSession.dto.response.PlaySessionResponse;
import com.emotory.backend.domain.playSession.service.PlaySessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/play-sessions")
public class PlaySessionController implements PlaySessionControllerDocs {

    private final PlaySessionService playSessionService;

    // 플레이 세션 생성
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
            @PathVariable Long playSessionId
    ) {

        playSessionService.end(
                playSessionId
        );
    }
}
