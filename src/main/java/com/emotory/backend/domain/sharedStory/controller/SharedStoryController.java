package com.emotory.backend.domain.sharedStory.controller;

import com.emotory.backend.domain.sharedStory.controller.docs.SharedStoryControllerDocs;
import com.emotory.backend.domain.sharedStory.dto.response.SharedStoryListResponse;
import com.emotory.backend.domain.sharedStory.dto.response.SharedStoryDetailResponse;
import com.emotory.backend.domain.sharedStory.service.SharedStoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SharedStoryController implements SharedStoryControllerDocs {

    private final SharedStoryService sharedStoryService;

    @Override
    @GetMapping("/shared-stories")
    public SharedStoryListResponse getStoryList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return sharedStoryService.getStoryList(page, size);
    }

    @Override
    @GetMapping("/shared-stories/{playSessionId}")
    public SharedStoryDetailResponse getStoryDetail(
            @PathVariable Long playSessionId
    ) {
        return sharedStoryService.getStoryDetail(playSessionId);
    }
}
