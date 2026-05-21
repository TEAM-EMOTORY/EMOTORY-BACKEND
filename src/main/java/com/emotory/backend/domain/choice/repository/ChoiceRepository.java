package com.emotory.backend.domain.choice.repository;

import com.emotory.backend.domain.choice.entity.Choice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {
    List<Choice> findByStoryNode_IdOrderByIdAsc(Long storyNodeId);
}
