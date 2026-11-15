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
import org.springframework.data.domain.PageRequest;
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

        // 공개와 같은 초안 → 동화 순서로 잠근다. 동화 잠금은 동시 재진입의 중복 생성을 막는다.
        var publishedDraft = publications.findByStoryId(request.storyId()).map(publication ->
                drafts.findLockedById(publication.getDraftId())
                        .orElseThrow(() -> new CustomException(ErrorCode.STORY_VERSION_UNAVAILABLE)));

        Story story =
                storyRepository.findLockedById(
                        request.storyId()

                ).orElseThrow(
                        StoryNotFoundException::new
                );

        // 기존 플레이는 이전 공개본·공개 중단 동화여도 이어갈 수 있다.
        // 상태를 초기화하지 않으므로 선택 이력과 생성 이미지도 같은 세션에서 유지된다.
        var resumable = playSessionRepository.findResumableSessions(
                request.memberId(), request.storyId(), PageRequest.of(0, 1));
        if (!resumable.isEmpty()) {
            return PlaySessionResponse.from(resumable.get(0));
        }

        // 새 세션을 만들 때만 최신 공개본과 공개 여부를 검사한다.
        if (publishedDraft.isPresent()
                && !Objects.equals(publishedDraft.get().getPublishedStoryId(), request.storyId())) {
            throw new CustomException(ErrorCode.STORY_VERSION_UNAVAILABLE);
        }
        if (!story.isAvailable()) {
            throw new CustomException(ErrorCode.STORY_UNPUBLISHED);
        }

        return createNewSession(member, story);
    }

    private PlaySessionResponse createNewSession(Member member, Story story) {
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
