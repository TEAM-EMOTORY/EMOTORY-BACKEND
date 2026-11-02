package com.emotory.backend.domain.authoring.repository;

import com.emotory.backend.domain.authoring.entity.StoryDraft;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface StoryDraftRepository extends JpaRepository<StoryDraft, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from StoryDraft d where d.id = :id")
    Optional<StoryDraft> findLockedById(@Param("id") Long id);
}
