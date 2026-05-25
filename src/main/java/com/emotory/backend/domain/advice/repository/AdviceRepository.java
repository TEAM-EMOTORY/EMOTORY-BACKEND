package com.emotory.backend.domain.advice.repository;

import com.emotory.backend.domain.advice.entity.Advice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdviceRepository extends JpaRepository<Advice, Long> {

    Optional<Advice> findFirstByStoryResult_IdOrderByIdDesc(Long storyResultId);

    List<Advice> findAllByStoryResult_IdOrderByIdAsc(Long storyResultId);
}
