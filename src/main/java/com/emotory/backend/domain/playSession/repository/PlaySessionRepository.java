package com.emotory.backend.domain.playSession.repository;

import com.emotory.backend.domain.playSession.entity.PlaySession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlaySessionRepository extends JpaRepository<PlaySession, Long> {
    boolean existsByStory_Id(Long storyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from PlaySession s where s.id = :id")
    Optional<PlaySession> findLockedById(@Param("id") Long id);
}
