package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.StudyGroupJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyGroupJpaRepository extends JpaRepository<StudyGroupJpaEntity, UUID> {

    Optional<StudyGroupJpaEntity> findBySlug(String slug);

    List<StudyGroupJpaEntity> findByCategoryIgnoreCase(String category, Pageable pageable);

    @Query("SELECT g FROM StudyGroupJpaEntity g WHERE g.author.id = :authorId")
    List<StudyGroupJpaEntity> findByAuthorId(@Param("authorId") UUID authorId);

    @Query("SELECT DISTINCT g FROM StudyGroupJpaEntity g JOIN GroupMembershipJpaEntity m ON m.group.id = g.id WHERE m.user.id = :userId AND m.status = 'APPROVED'")
    List<StudyGroupJpaEntity> findByMemberId(@Param("userId") UUID userId);

    boolean existsBySlug(String slug);
}
