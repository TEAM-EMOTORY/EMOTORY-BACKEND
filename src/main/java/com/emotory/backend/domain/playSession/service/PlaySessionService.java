package com.emotory.backend.domain.playSession.service;

import com.emotory.backend.domain.member.entity.Member;
import com.emotory.backend.global.exception.member.MemberNotFoundException;
import com.emotory.backend.domain.member.repository.MemberRepository;
import com.emotory.backend.domain.playSession.dto.request.PlaySessionCreateRequest;
import com.emotory.backend.domain.playSession.dto.response.PlaySessionResponse;
import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.playSession.repository.PlaySessionRepository;
import com.emotory.backend.domain.story.entity.Story;
import com.emotory.backend.domain.story.repository.StoryRepository;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import com.emotory.backend.domain.storyNode.repository.StoryNodeRepository;
import com.emotory.backend.global.exception.playSession.PlaySessionNotFoundException;
import com.emotory.backend.global.exception.storyNode.StoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaySessionService {

    private final PlaySessionRepository playSessionRepository;
    private final MemberRepository memberRepository;
    private final StoryRepository storyRepository;
    private final StoryNodeRepository storyNodeRepository;

    @Transactional
    public PlaySessionResponse create(
            PlaySessionCreateRequest request
    ) {

        Member member =
                memberRepository.findById(
                        request.memberId()
                ).orElseThrow(
                        MemberNotFoundException::new
                );

        Story story =
                storyRepository.findById(
                        request.storyId()

                ).orElseThrow(
                        StoryNotFoundException::new
                );

        StoryNode startNode =
                storyNodeRepository
                        .findFirstByStoryOrderByNodeOrderAsc(
                                story
                        ).orElseThrow();

        PlaySession playSession =
                new PlaySession(
                        member,
                        story,
                        startNode
                );

        playSessionRepository.save(
                playSession
        );

        return PlaySessionResponse.from(
                playSession
        );
    }

    public PlaySessionResponse get(
            Long playSessionId
    ) {

        PlaySession playSession =
                playSessionRepository.findById(
                        playSessionId
                ).orElseThrow(
                        PlaySessionNotFoundException::new
                );

        return PlaySessionResponse.from(
                playSession
        );
    }

    @Transactional
    public void end(
            Long playSessionId
    ) {

        PlaySession playSession =
                playSessionRepository.findById(
                        playSessionId
                ).orElseThrow(
                        PlaySessionNotFoundException::new
                );

        playSession.end();
    }
}
