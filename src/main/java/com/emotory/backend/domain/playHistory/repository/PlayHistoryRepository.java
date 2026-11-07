package com.emotory.backend.domain.playHistory.repository;

import com.emotory.backend.domain.playHistory.entity.PlayHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    @Query("select coalesce(max(h.stepNumber), 0) from PlayHistory h where h.playSession.id = :sessionId")
    int findLastStep(@Param("sessionId") Long sessionId);

    int countByPlaySession_Id(Long playSessionId);

    List<PlayHistory> findByPlaySession_IdOrderByStepNumberAsc(Long playSessionId);
}
