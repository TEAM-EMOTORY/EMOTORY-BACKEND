package com.emotory.backend.domain.playSession.repository;

import com.emotory.backend.domain.playSession.entity.PlaySession;
import com.emotory.backend.domain.sharedStory.dto.response.SharedStoryListItemResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlaySessionRepository extends JpaRepository<PlaySession, Long> {
    boolean existsByStory_Id(Long storyId);

    @Query("""
            select new com.emotory.backend.domain.sharedStory.dto.response.SharedStoryListItemResponse(
                s.id, m.name, story.emotion, story.title, image.imageUrl)
            from PlaySession s
            join s.member m
            join s.story story
            join s.currentNode node
            join GeneratedImage image on image.playSession = s and image.storyNode = node
            where s.shared = true
                and s.status = com.emotory.backend.domain.playSession.entity.PlaySessionStatus.ENDED
                and node.isEnding = true
                and image.generatedImageId = (
                    select min(g.generatedImageId) from GeneratedImage g
                    where g.playSession = s and g.storyNode = node)
            order by s.createdAt desc, s.id desc
            """)
    Slice<SharedStoryListItemResponse> findSharedStories(Pageable pageable);

    @EntityGraph(attributePaths = {"member", "story", "currentNode"})
    @Query("""
            select s from PlaySession s
            where s.id = :id and s.shared = true
                and s.status = com.emotory.backend.domain.playSession.entity.PlaySessionStatus.ENDED
                and s.currentNode.isEnding = true
            """)
    Optional<PlaySession> findSharedStoryById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from PlaySession s where s.id = :id")
    Optional<PlaySession> findLockedById(@Param("id") Long id);
}
