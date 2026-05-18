package com.emotory.backend.domain.playHistory.service;

import com.emotory.backend.domain.playHistory.dto.response.PlayHistoryListResponse;
import com.emotory.backend.domain.playHistory.dto.response.PlayHistoryResponse;
import com.emotory.backend.domain.playHistory.repository.PlayHistoryRepository;
import com.emotory.backend.domain.playSession.repository.PlaySessionRepository;
import com.emotory.backend.global.exception.playSesstion.PlaySessionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayHistoryService {

    private final PlayHistoryRepository playHistoryRepository;
    //@TODO PlaySession PR 머지 후 import
    private final PlaySessionRepository playSessionRepository;

    public PlayHistoryListResponse getPlayHistories(Long playSessionId) {
        if (!playSessionRepository.existsById(playSessionId)) {
            throw new PlaySessionNotFoundException();
        }

        //@TODO playSession PR 머지 후 연관관계 기준으로 동작 확인
        List<PlayHistoryResponse> histories = playHistoryRepository
                .findByPlaySession_IdOrderByStepNumberAsc(playSessionId)
                .stream()
                .map(PlayHistoryResponse::from)
                .toList();

        return PlayHistoryListResponse.from(histories);
    }
}
