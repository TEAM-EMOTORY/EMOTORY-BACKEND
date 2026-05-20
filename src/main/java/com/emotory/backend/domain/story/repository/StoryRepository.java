package com.emotory.backend.domain.story.repository;


import com.emotory.backend.domain.story.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, Long> {
}
