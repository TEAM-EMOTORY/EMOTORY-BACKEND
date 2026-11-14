package com.emotory.backend.domain.generatedImage.repository;

import com.emotory.backend.domain.generatedImage.entity.GeneratedImage;
import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GeneratedImageRepository extends JpaRepository<GeneratedImage, Long> {

    List<GeneratedImage> findByPlaySession_Id(Long playSessionId);

    Optional<GeneratedImage> findByPlaySessionAndStoryNode(
            PlaySession playSession,
            StoryNode storyNode
    );
}
