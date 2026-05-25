package com.emotory.backend.domain.interactiveStory.service;

import com.emotory.backend.domain.choice.entity.Choice;
import com.emotory.backend.domain.choice.repository.ChoiceRepository;
import com.emotory.backend.domain.interactiveStory.dto.request.*;
import com.emotory.backend.domain.interactiveStory.dto.response.InteractiveStoryCreateResponse;
import com.emotory.backend.domain.interactiveStory.entity.StoryNodeType;
import com.emotory.backend.domain.story.entity.Story;
import com.emotory.backend.domain.story.repository.StoryRepository;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import com.emotory.backend.domain.storyNode.repository.StoryNodeRepository;
import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;
import com.emotory.backend.global.exception.storyNode.StoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class InteractiveStoryService {

    private final StoryRepository storyRepository;
    private final StoryNodeRepository storyNodeRepository;
    private final ChoiceRepository choiceRepository;

    public InteractiveStoryCreateResponse create(InteractiveStoryCreateRequest request) {
        validateStory(request);

        Story story = Story.create(
                request.title(),
                request.description(),
                request.emotion()
        );

        Story savedStory = storyRepository.save(story);

        Map<String, StoryNode> nodeMap = new HashMap<>();

        int nodeOrder = 1;

        for (StoryNodeRequest nodeRequest : request.story().nodes()) {
            boolean isEnding = nodeRequest.type() == StoryNodeType.ENDING;

            String content = isEnding
                    ? buildEndingContent(nodeRequest.ending())
                    : nodeRequest.content();

            StoryNode storyNode = StoryNode.create(
                    savedStory,
                    content,
                    nodeOrder++,
                    isEnding,
                    request.emotion()
            );

            StoryNode savedNode = storyNodeRepository.save(storyNode);
            nodeMap.put(nodeRequest.id(), savedNode);
        }

        for (StoryNodeRequest nodeRequest : request.story().nodes()) {
            if (nodeRequest.choices() == null || nodeRequest.choices().isEmpty()) {
                continue;
            }

            StoryNode currentNode = nodeMap.get(nodeRequest.id());

            for (StoryChoiceRequest choiceRequest : nodeRequest.choices()) {
                StoryNode nextNode = nodeMap.get(choiceRequest.nextNodeId());

                Choice choice = Choice.create(
                        currentNode,
                        choiceRequest.text(),
                        nextNode
                );

                choiceRepository.save(choice);
            }
        }

        return new InteractiveStoryCreateResponse(savedStory.getId());
    }

    private void validateStory(InteractiveStoryCreateRequest request) {
        Map<String, StoryNodeRequest> nodeMap = new HashMap<>();

        for (StoryNodeRequest node : request.story().nodes()) {
            if (nodeMap.containsKey(node.id())) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            nodeMap.put(node.id(), node);
        }

        if (!nodeMap.containsKey(request.story().rootNodeId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        for (StoryNodeRequest node : request.story().nodes()) {
            if (node.type() == StoryNodeType.STORY) {
                if (node.content() == null || node.content().isBlank()) {
                    throw new CustomException(ErrorCode.INVALID_INPUT);
                }

                if (node.choices() == null || node.choices().isEmpty()) {
                    throw new CustomException(ErrorCode.INVALID_INPUT);
                }

                for (StoryChoiceRequest choice : node.choices()) {
                    if (!nodeMap.containsKey(choice.nextNodeId())) {
                        throw new CustomException(ErrorCode.INVALID_INPUT);
                    }
                }
            }

            if (node.type() == StoryNodeType.ENDING && node.ending() == null) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
        }
    }

    private String buildEndingContent(StoryEndingRequest ending) {
        String advices = ending.advices()
                .stream()
                .map(advice -> "조언: " + advice.title() + " | " + advice.description())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");

        return String.join("\n",
                ending.summary(),
                "느낀 감정: " + ending.emotionLabel(),
                "내용 요약: " + ending.contentSummary(),
                advices
        );
    }

    public void deleteStory(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(StoryNotFoundException::new);

        List<StoryNode> storyNodes = storyNodeRepository.findAllByStory(story);

        if (!storyNodes.isEmpty()) {
            choiceRepository.deleteAllByStoryNodeIn(storyNodes);
            storyNodeRepository.deleteAll(storyNodes);
        }

        storyRepository.delete(story);
    }
}