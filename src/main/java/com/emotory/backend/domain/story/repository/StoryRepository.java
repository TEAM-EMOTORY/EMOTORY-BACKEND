package com.emotory.backend.domain.story.repository;


import com.emotory.backend.domain.story.entity.Story;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoryRepository extends JpaRepository<Story, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Story s where s.id = :id")
    Optional<Story> findLockedById(@Param("id") Long id);
}
