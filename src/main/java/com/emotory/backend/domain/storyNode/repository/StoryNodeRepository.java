package com.emotory.backend.domain.storyNode.repository;

import com.emotory.backend.domain.storyNode.entity.StoryNode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryNodeRepository extends JpaRepository<StoryNode, Long> {
}
