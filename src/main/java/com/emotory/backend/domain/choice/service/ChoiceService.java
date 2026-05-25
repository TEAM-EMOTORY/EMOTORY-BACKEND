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
import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;
import com.emotory.backend.global.exception.choice.ChoiceNotFoundException;
import com.emotory.backend.global.exception.playSession.PlaySessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

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

        if (Boolean.TRUE.equals(playSession.getCurrentNode().getIsEnding())) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        if (!choice.getStoryNode().getId().equals(playSession.getCurrentNode().getId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

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

        StoryResult storyResult = StoryResult.of(
                summary,
                emotion,
                playSession
        );

        StoryResult savedStoryResult = storyResultRepository.save(
                storyResult
        );

        List<Advice> advices = extractAdvices(
                lines,
                savedStoryResult
        );

        adviceRepository.saveAll(
                advices
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

    private List<Advice> extractAdvices(
            String[] lines,
            StoryResult storyResult
    ) {
        return Arrays.stream(lines)
                .filter(line -> line.startsWith("조언:"))
                .map(line -> line.substring("조언:".length()).trim())
                .map(line -> {
                    String[] parts = line.split("\\|", 2);

                    String title = parts.length > 0 && !parts[0].isBlank()
                            ? parts[0].trim()
                            : "감정 조언";

                    String description = parts.length > 1
                            ? parts[1].trim()
                            : "";

                    return Advice.of(
                            title,
                            description,
                            storyResult
                    );
                })
                .toList();
    }
}