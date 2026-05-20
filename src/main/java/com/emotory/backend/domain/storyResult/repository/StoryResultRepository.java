package com.emotory.backend.domain.storyResult.repository;

import com.emotory.backend.domain.storyResult.entity.StoryResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryResultRepository extends JpaRepository<StoryResult, Long> {

    Optional<StoryResult> findFirstByPlaySession_IdOrderByCreatedAtDesc(Long playSessionId);
}
