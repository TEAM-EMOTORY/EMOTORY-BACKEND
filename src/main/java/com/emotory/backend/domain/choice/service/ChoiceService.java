package com.emotory.backend.domain.choice.service;

import com.emotory.backend.domain.choice.dto.request.ChoiceSelectRequest;
import com.emotory.backend.domain.choice.dto.response.ChoiceSelectResponse;
import com.emotory.backend.domain.choice.entity.Choice;
import com.emotory.backend.domain.choice.repository.ChoiceRepository;
import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.playSession.repository.PlaySessionRepository;
import com.emotory.backend.global.exception.Choice.ChoiceNotFoundException;
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

        // 다음 노드 이동
        playSession.changeCurrentNode(
                choice.getNextNode()
        );

        return ChoiceSelectResponse.from(
                playSession
        );
    }
}
