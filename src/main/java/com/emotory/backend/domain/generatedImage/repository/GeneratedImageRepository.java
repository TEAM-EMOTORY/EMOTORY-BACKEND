package com.emotory.backend.domain.generatedImage.repository;

import com.emotory.backend.domain.generatedImage.entity.GeneratedImage;
import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface GeneratedImageRepository extends JpaRepository<GeneratedImage, Long> {

    List<GeneratedImage> findByPlaySession_Id(Long playSessionId);

    @Query("""
            select image from GeneratedImage image
            join SessionImageContext context on context.playSessionId = image.playSession.id
            where context.referenceImageKey = :referenceKey and image.storyNode = :node
            order by image.generatedImageId asc
            """)
    List<GeneratedImage> findReusableImages(@Param("referenceKey") String referenceKey,
                                            @Param("node") StoryNode node,
                                            Pageable pageable);

    Optional<GeneratedImage> findByPlaySessionAndStoryNode(
            PlaySession playSession,
            StoryNode storyNode
    );
}
