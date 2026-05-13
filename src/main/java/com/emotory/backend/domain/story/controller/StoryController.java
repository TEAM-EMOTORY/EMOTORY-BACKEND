package com.emotory.backend.domain.story.controller;

import com.emotory.backend.domain.story.controller.docs.StoryControllerDocs;
import com.emotory.backend.domain.story.dto.response.StoryListResponse;
import com.emotory.backend.domain.story.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stories")
public class StoryController implements StoryControllerDocs {

    private final StoryService storyService;

    @Override
    @GetMapping
    public StoryListResponse getStories() {
        return storyService.getStories();
    }
}
