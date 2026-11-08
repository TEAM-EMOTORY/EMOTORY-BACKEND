package com.emotory.backend.domain.authoring.repository;

import com.emotory.backend.domain.authoring.entity.StoryPublication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StoryPublicationRepository extends JpaRepository<StoryPublication, Long> {
    Optional<StoryPublication> findByStoryId(Long storyId);
    boolean existsByStoryId(Long storyId);
}
