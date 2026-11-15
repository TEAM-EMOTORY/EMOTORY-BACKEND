package com.emotory.backend.domain.sharedStory.service;

import com.emotory.backend.domain.generatedImage.entity.GeneratedImage;
import com.emotory.backend.domain.generatedImage.repository.GeneratedImageRepository;
import com.emotory.backend.domain.playHistory.entity.PlayHistory;
import com.emotory.backend.domain.playHistory.repository.PlayHistoryRepository;
import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.playSession.repository.PlaySessionRepository;
import com.emotory.backend.domain.sharedStory.dto.response.SharedStoryListResponse;
import com.emotory.backend.domain.sharedStory.dto.response.SharedStoryDetailResponse;
import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedStoryService {

    private final PlaySessionRepository playSessionRepository;
    private final PlayHistoryRepository playHistoryRepository;
    private final GeneratedImageRepository generatedImageRepository;

    public SharedStoryListResponse getStoryList(int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        var stories = playSessionRepository.findSharedStories(PageRequest.of(page, size));
        return new SharedStoryListResponse(stories.getContent(), page, size, stories.hasNext());
    }

    public SharedStoryDetailResponse getStoryDetail(Long playSessionId) {
        PlaySession playSession = playSessionRepository.findSharedStoryById(playSessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SHARED_STORY_NOT_FOUND));

        Map<Long, String> imageUrls = generatedImageRepository.findByPlaySession_Id(playSessionId)
                .stream()
                .sorted(Comparator.comparing(GeneratedImage::getGeneratedImageId))
                .collect(Collectors.toMap(image -> image.getStoryNode().getId(), GeneratedImage::getImageUrl,
                        (first, duplicate) -> first));

        var nodes = new ArrayList<SharedStoryDetailResponse.Node>();
        for (PlayHistory history : playHistoryRepository.findByPlaySession_IdOrderByStepNumberAsc(playSessionId)) {
            var node = history.getStoryNode();
            nodes.add(SharedStoryDetailResponse.Node.from(
                    node, imageUrls.get(node.getId())
            ));
        }

        var endingNode = playSession.getCurrentNode();
        nodes.add(SharedStoryDetailResponse.Node.from(
                endingNode, imageUrls.get(endingNode.getId())
        ));

        return SharedStoryDetailResponse.from(playSession, nodes);
    }
}
