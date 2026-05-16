package com.emotory.backend.domain.playSession.repository;

import com.emotory.backend.domain.playSession.entity.PlaySession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaySessionRepository extends JpaRepository<PlaySession, Long> {
}
