package com.emotory.backend.domain.playSession.service;

import com.emotory.backend.domain.member.entity.Member;
import com.emotory.backend.domain.generatedImage.repository.GeneratedImageRepository;
import com.emotory.backend.domain.playHistory.repository.PlayHistoryRepository;
import com.emotory.backend.domain.authoring.repository.StoryDraftRepository;
import com.emotory.backend.domain.authoring.repository.StoryPublicationRepository;
import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;
import com.emotory.backend.global.exception.member.MemberNotFoundException;
import com.emotory.backend.domain.member.repository.MemberRepository;
import com.emotory.backend.domain.playSession.dto.request.PlaySessionCreateRequest;
import com.emotory.backend.domain.playSession.dto.request.PlaySessionEndRequest;
import com.emotory.backend.domain.playSession.dto.response.PlaySessionResponse;
import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.playSession.entity.PlaySessionStatus;
import com.emotory.backend.domain.playSession.repository.PlaySessionRepository;
import com.emotory.backend.domain.story.entity.Story;
import com.emotory.backend.domain.story.repository.StoryRepository;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import com.emotory.backend.domain.storyNode.repository.StoryNodeRepository;
import com.emotory.backend.global.exception.playSession.PlaySessionNotFoundException;
import com.emotory.backend.global.exception.storyNode.StoryNodeNotFoundException;
import com.emotory.backend.global.exception.storyNode.StoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaySessionService {

    private final PlaySessionRepository playSessionRepository;
    private final MemberRepository memberRepository;
    private final StoryRepository storyRepository;
    private final StoryNodeRepository storyNodeRepository;
    private final StoryPublicationRepository publications;
    private final StoryDraftRepository drafts;
    private final PlayHistoryRepository playHistoryRepository;
    private final GeneratedImageRepository generatedImageRepository;

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

        // 공개와 동일한 초안 잠금을 먼저 획득한 뒤 동화 잠금을 획득한다.
        // 재공개가 먼저 끝났다면 최신 공개 ID를 확인하고 이전 버전의 새 플레이를 거절한다.
        publications.findByStoryId(request.storyId()).ifPresent(publication -> {
            var draft = drafts.findLockedById(publication.getDraftId())
                    .orElseThrow(() -> new CustomException(ErrorCode.STORY_VERSION_UNAVAILABLE));
            if (!Objects.equals(draft.getPublishedStoryId(), request.storyId())) {
                throw new CustomException(ErrorCode.STORY_VERSION_UNAVAILABLE);
            }
        });

        Story story =
                storyRepository.findLockedById(
                        request.storyId()

                ).orElseThrow(
                        StoryNotFoundException::new
                );

        if (!story.isAvailable()) {
            throw new CustomException(ErrorCode.STORY_UNPUBLISHED);
        }

        StoryNode startNode =
                storyNodeRepository
                        .findFirstByStoryOrderByNodeOrderAsc(
                                story
                        ).orElseThrow(
                                StoryNodeNotFoundException::new
                        );

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
            Long playSessionId,
            PlaySessionEndRequest request
    ) {

        PlaySession playSession =
                playSessionRepository.findLockedById(
                        playSessionId
                ).orElseThrow(
                        PlaySessionNotFoundException::new
                );

        if (playSession.getCurrentNode() == null
                || !Boolean.TRUE.equals(playSession.getCurrentNode().getIsEnding())) {
            throw new CustomException(ErrorCode.PLAY_SESSION_NOT_AT_ENDING);
        }

        if (playSession.getStatus() == PlaySessionStatus.ENDED) {
            if (playSession.isShared() != request.shared()) {
                throw new CustomException(ErrorCode.PLAY_SESSION_ALREADY_ENDED);
            }
            return;
        }
        if (request.shared()) {
            validateImagesForSharing(playSession);
        }
        playSession.end(request.shared());
    }

    private void validateImagesForSharing(PlaySession playSession) {
        var nodesWithImages = generatedImageRepository.findByPlaySession_Id(playSession.getId())
                .stream()
                .filter(image -> image.getImageUrl() != null && !image.getImageUrl().isBlank())
                .map(image -> image.getStoryNode().getId())
                .collect(Collectors.toSet());

        boolean hasMissingImage = !nodesWithImages.contains(playSession.getCurrentNode().getId())
                || playHistoryRepository.findByPlaySession_IdOrderByStepNumberAsc(playSession.getId())
                .stream()
                .anyMatch(history -> !nodesWithImages.contains(history.getStoryNode().getId()));
        if (hasMissingImage) {
            throw new CustomException(ErrorCode.STORY_SHARING_NOT_READY);
        }
    }
}
