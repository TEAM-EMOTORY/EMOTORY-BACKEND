package com.emotory.backend.domain.story.repository;


import com.emotory.backend.domain.story.entity.Story;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {
    @Query("""
            select s from Story s
            where not exists (select p.id from StoryPublication p where p.storyId = s.id)
               or exists (
                   select p.id from StoryPublication p, StoryDraft d
                   where p.storyId = s.id and p.draftId = d.id and d.publishedStoryId = s.id
               )
            order by s.id asc
            """)
    List<Story> findAvailableStories();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Story s where s.id = :id")
    Optional<Story> findLockedById(@Param("id") Long id);
}
