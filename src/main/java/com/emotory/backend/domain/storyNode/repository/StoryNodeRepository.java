package com.emotory.backend.domain.storyNode.repository;

import com.emotory.backend.domain.story.entity.Story;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryNodeRepository extends JpaRepository<StoryNode,Long> {

    Optional<StoryNode>
    findFirstByStoryOrderByNodeOrderAsc(
            Story story
    );
}
