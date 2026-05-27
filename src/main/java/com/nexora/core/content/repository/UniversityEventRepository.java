package com.nexora.core.content.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nexora.core.content.entity.UniversityEvent;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface UniversityEventRepository extends JpaRepository<UniversityEvent, UUID> {
    Optional<UniversityEvent> findBySlug(String slug);

    Page<UniversityEvent> findByCategoryIgnoreCase(String category, Pageable pageable);

    @Query(value = "SELECT e.id FROM university_events e JOIN event_attendees ea ON e.id = ea.event_id WHERE e.id IN :eventIds AND ea.user_id = :userId", nativeQuery = true)
    List<UUID> findRegisteredEventIds(@Param("eventIds") List<UUID> eventIds, @Param("userId") UUID userId);
}
