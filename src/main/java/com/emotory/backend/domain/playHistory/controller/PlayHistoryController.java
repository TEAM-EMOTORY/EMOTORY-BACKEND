package com.emotory.backend.domain.playHistory.controller;

import com.emotory.backend.domain.playHistory.dto.response.PlayHistoryListResponse;
import com.emotory.backend.domain.playHistory.service.PlayHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/play-sessions/{playSessionId}/histories")
public class PlayHistoryController {

    private final PlayHistoryService playHistoryService;

    @GetMapping
    public PlayHistoryListResponse getPlayHistories(
            @PathVariable Long playSessionId
    ) {
        return playHistoryService.getPlayHistories(playSessionId);
    }
}
