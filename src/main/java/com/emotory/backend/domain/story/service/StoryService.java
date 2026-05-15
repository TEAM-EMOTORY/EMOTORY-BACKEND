package com.emotory.backend.domain.story.service;

import com.emotory.backend.domain.story.dto.response.StoryListResponse;
import com.emotory.backend.domain.story.dto.response.StoryResponse;
import com.emotory.backend.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryService {

    private final StoryRepository storyRepository;

    public StoryListResponse getStories() {
        List<StoryResponse> stories = storyRepository.findAll()
                .stream()
                .map(StoryResponse::from)
                .toList();

        return StoryListResponse.from(stories);
    }
}
