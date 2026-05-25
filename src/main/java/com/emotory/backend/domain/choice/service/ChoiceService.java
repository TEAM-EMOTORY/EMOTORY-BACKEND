package com.emotory.backend.domain.choice.service;

import com.emotory.backend.domain.advice.entity.Advice;
import com.emotory.backend.domain.advice.repository.AdviceRepository;
import com.emotory.backend.domain.choice.dto.request.ChoiceSelectRequest;
import com.emotory.backend.domain.choice.dto.response.ChoiceSelectResponse;
import com.emotory.backend.domain.choice.entity.Choice;
import com.emotory.backend.domain.choice.repository.ChoiceRepository;
import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.playSession.repository.PlaySessionRepository;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import com.emotory.backend.domain.storyResult.entity.StoryResult;
import com.emotory.backend.domain.storyResult.repository.StoryResultRepository;
import com.emotory.backend.global.exception.choice.ChoiceNotFoundException;
import com.emotory.backend.global.exception.playSession.PlaySessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChoiceService {

    private final ChoiceRepository choiceRepository;
    private final PlaySessionRepository playSessionRepository;
    private final StoryResultRepository storyResultRepository;
    private final AdviceRepository adviceRepository;

    @Transactional
    public ChoiceSelectResponse selectChoice(
            Long playSessionId,
            ChoiceSelectRequest request
    ) {

        PlaySession playSession =
                playSessionRepository.findById(
                        playSessionId
                ).orElseThrow(
                        PlaySessionNotFoundException::new
                );

        Choice choice =
                choiceRepository.findById(
                        request.choiceId()
                ).orElseThrow(
                        ChoiceNotFoundException::new
                );

        StoryNode nextNode = choice.getNextNode();

        playSession.changeCurrentNode(
                nextNode
        );

        if (Boolean.TRUE.equals(nextNode.getIsEnding())) {
            createStoryResultFromEndingNode(
                    playSession,
                    nextNode
            );
        }

        return ChoiceSelectResponse.from(
                playSession
        );
    }

    private void createStoryResultFromEndingNode(
            PlaySession playSession,
            StoryNode endingNode
    ) {
        if (storyResultRepository.existsByPlaySession_Id(playSession.getId())) {
            return;
        }

        String[] lines = endingNode.getContent().split("\n");

        String summary = lines.length > 0
                ? lines[0].trim()
                : endingNode.getContent();

        String emotion = extractValue(
                lines,
                "느낀 감정:"
        );

        String advice = extractValue(
                lines,
                "이런 감정이 들 때는요:"
        );

        StoryResult storyResult = StoryResult.of(
                summary,
                emotion,
                playSession
        );

        StoryResult savedStoryResult = storyResultRepository.save(
                storyResult
        );

        Advice adviceEntity = Advice.of(
                "감정 조언",
                advice,
                savedStoryResult
        );

        adviceRepository.save(
                adviceEntity
        );
    }

    private String extractValue(
            String[] lines,
            String prefix
    ) {
        for (String line : lines) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }

        return "";
    }
}
