package com.emotory.backend.domain.playHistory.repository;

import com.emotory.backend.domain.playHistory.entity.PlayHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    int countByPlaySession_Id(Long playSessionId);

    List<PlayHistory> findByPlaySession_IdOrderByStepNumberAsc(Long playSessionId);
}
