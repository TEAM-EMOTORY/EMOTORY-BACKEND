package com.emotory.backend.domain.storyNode.controller;

import com.emotory.backend.domain.storyNode.controller.docs.StoryNodeControllerDocs;
import com.emotory.backend.domain.storyNode.dto.response.StoryNodeResponse;
import com.emotory.backend.domain.storyNode.service.StoryNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/story-nodes")
public class StoryNodeController implements StoryNodeControllerDocs {

    private final StoryNodeService storyNodeService;

    @Override
    @GetMapping("/{storyNodeId}")
    public StoryNodeResponse getStoryNode(@PathVariable Long storyNodeId) {
        return storyNodeService.getStoryNode(storyNodeId);
    }
}
