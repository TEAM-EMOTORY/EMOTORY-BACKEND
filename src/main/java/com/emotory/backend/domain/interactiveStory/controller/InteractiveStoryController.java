package com.emotory.backend.domain.interactiveStory.controller;

import com.emotory.backend.domain.interactiveStory.dto.request.InteractiveStoryCreateRequest;
import com.emotory.backend.domain.interactiveStory.dto.response.InteractiveStoryCreateResponse;
import com.emotory.backend.domain.interactiveStory.service.InteractiveStoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/interactive-stories")
public class InteractiveStoryController {

    private final InteractiveStoryService interactiveStoryService;

    @PostMapping
    public InteractiveStoryCreateResponse create(
            @Valid @RequestBody InteractiveStoryCreateRequest request
    ) {
        return interactiveStoryService.create(request);
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long storyId
    ) {
        interactiveStoryService.deleteStory(storyId);
        return ResponseEntity.noContent().build();
    }
}