package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.UniversityEventJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UniversityEventJpaRepository extends JpaRepository<UniversityEventJpaEntity, UUID> {
    Optional<UniversityEventJpaEntity> findBySlug(String slug);

    Page<UniversityEventJpaEntity> findByCategoryIgnoreCase(String category, Pageable pageable);

    @Query(value = "SELECT e.id FROM university_events e JOIN event_attendees ea ON e.id = ea.event_id WHERE e.id IN :eventIds AND ea.user_id = :userId", nativeQuery = true)
    List<UUID> findRegisteredEventIds(@Param("eventIds") List<UUID> eventIds, @Param("userId") UUID userId);
}
