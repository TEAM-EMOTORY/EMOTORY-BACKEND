package com.emotory.backend.domain.choice.repository;

import com.emotory.backend.domain.choice.entity.Choice;
import com.emotory.backend.domain.storyNode.entity.StoryNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {
    @EntityGraph(attributePaths = {"storyNode", "nextNode"})
    List<Choice> findByStoryNodeInOrderByIdAsc(List<StoryNode> nodes);

    List<Choice> findByStoryNode_IdOrderByIdAsc(Long storyNodeId);

    void deleteAllByStoryNodeIn(List<StoryNode> storyNodes);
}
