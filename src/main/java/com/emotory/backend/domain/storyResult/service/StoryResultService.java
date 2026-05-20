package com.emotory.backend.domain.storyResult.service;

import com.emotory.backend.domain.advice.entity.Advice;
import com.emotory.backend.global.exception.advice.AdviceNotFoundException;
import com.emotory.backend.domain.advice.repository.AdviceRepository;
import com.emotory.backend.domain.generatedImage.entity.GeneratedImage;
import com.emotory.backend.domain.generatedImage.repository.GeneratedImageRepository;
import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.playSession.repository.PlaySessionRepository;
import com.emotory.backend.domain.storyResult.dto.request.StoryResultCreateRequest;
import com.emotory.backend.domain.storyResult.dto.response.StoryResultResponse;
import com.emotory.backend.domain.storyResult.entity.StoryResult;
import com.emotory.backend.domain.storyResult.exception.StoryResultNotFoundException;
import com.emotory.backend.domain.storyResult.repository.StoryResultRepository;
import com.emotory.backend.global.exception.playSession.PlaySessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryResultService {

    //@ TODO PlaySession PR 머지후 동작 확인
    private final PlaySessionRepository playSessionRepository;
    private final StoryResultRepository storyResultRepository;
    private final AdviceRepository adviceRepository;

    //@ TODO 이미지 생성 PR 머지후 동작 확인
    private final GeneratedImageRepository generatedImageRepository;

    @Transactional
    public void createStoryResult(
            Long playSessionId,
            StoryResultCreateRequest request
    ) {
        PlaySession playSession = playSessionRepository.findById(playSessionId)
                .orElseThrow(PlaySessionNotFoundException::new);

        StoryResult storyResult = StoryResult.of(
                request.summary(),
                request.emotion(),
                playSession
        );
        StoryResult savedStoryResult = storyResultRepository.save(storyResult);

        Advice advice = Advice.of(
                request.advice(),
                savedStoryResult
        );
        adviceRepository.save(advice);
    }

    public StoryResultResponse getStoryResult(Long playSessionId) {
        PlaySession playSession = playSessionRepository.findById(playSessionId)
                .orElseThrow(PlaySessionNotFoundException::new);

        StoryResult storyResult = storyResultRepository
                .findFirstByPlaySession_IdOrderByCreatedAtDesc(playSessionId)
                .orElseThrow(StoryResultNotFoundException::new);

        Advice advice = adviceRepository
                .findFirstByStoryResult_IdOrderByIdDesc(storyResult.getId())
                .orElseThrow(AdviceNotFoundException::new);

        String generatedImageUrl = generatedImageRepository
                .findByPlaySessionAndStoryNode(
                        playSession,
                        playSession.getCurrentNode()
                )
                .map(GeneratedImage::getImageUrl)
                .orElse(null);

        return new StoryResultResponse(
                storyResult.getSummary(),
                storyResult.getEmotion(),
                advice.getDescription(),
                generatedImageUrl
        );
    }
}
