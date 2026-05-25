package com.emotory.backend.domain.storyNode.service;

import com.emotory.backend.domain.choice.repository.ChoiceRepository;
import com.emotory.backend.domain.storyNode.dto.response.StoryNodeResponse;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import com.emotory.backend.domain.storyNode.repository.StoryNodeRepository;
import com.emotory.backend.global.exception.storyNode.StoryNodeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryNodeService {

    private final StoryNodeRepository storyNodeRepository;
    private final ChoiceRepository choiceRepository;

    public StoryNodeResponse getStoryNode(Long storyNodeId) {
        StoryNode storyNode = storyNodeRepository.findById(storyNodeId)
                .orElseThrow(StoryNodeNotFoundException::new);

        List<StoryNodeResponse.ChoiceResponse> choices = choiceRepository.findByStoryNode_IdOrderByIdAsc(storyNodeId)
                .stream()
                .map(choice -> new StoryNodeResponse.ChoiceResponse(
                        choice.getId(),
                        choice.getContent()
                ))
                .toList();

        return new StoryNodeResponse(
                storyNode.getId(),
                storyNode.getContent(),
                storyNode.getNodeOrder(),
                storyNode.getIsEnding(),
                choices
        );
    }
}
