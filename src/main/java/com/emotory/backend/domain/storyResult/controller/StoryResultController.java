package com.emotory.backend.domain.storyResult.controller;

import com.emotory.backend.domain.storyResult.dto.request.StoryResultCreateRequest;
import com.emotory.backend.domain.storyResult.dto.response.StoryResultResponse;
import com.emotory.backend.domain.storyResult.service.StoryResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/play-sessions")
public class StoryResultController {

    private final StoryResultService storyResultService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{playSessionId}/results")
    public void createStoryResult(
            @PathVariable Long playSessionId,
            @Valid @RequestBody StoryResultCreateRequest request
    ) {
        storyResultService.createStoryResult(
                playSessionId,
                request
        );
    }

    @GetMapping("/{playSessionId}/results")
    public StoryResultResponse getStoryResult(
            @PathVariable Long playSessionId
    ) {
        return storyResultService.getStoryResult(playSessionId);
    }
}
