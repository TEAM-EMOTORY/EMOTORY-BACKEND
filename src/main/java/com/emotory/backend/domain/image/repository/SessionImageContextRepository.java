package com.emotory.backend.domain.image.repository;

import com.emotory.backend.domain.image.entity.SessionImageContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SessionImageContextRepository extends JpaRepository<SessionImageContext, Long> {
    @Query("""
            select context from SessionImageContext context
            join PlaySession session on session.id = context.playSessionId
            where session.member.name = :name and session.member.faceImageSha256 = :sha256
                and session.story.id = :storyId and context.referenceImageKey is not null
            order by context.playSessionId asc
            """)
    List<SessionImageContext> findReusableContexts(@Param("name") String name,
                                                  @Param("sha256") String sha256,
                                                  @Param("storyId") Long storyId,
                                                  Pageable pageable);
}
