package com.nexora.core.domain.content.repositories;

import com.nexora.core.domain.content.aggregates.UniversityEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    List<UniversityEvent> findAll(int limit, int offset, String category);
    Optional<UniversityEvent> findBySlug(String slug);
    Optional<UniversityEvent> findById(UUID id);
    UniversityEvent save(UniversityEvent event);
    void deleteById(UUID id);
    boolean existsBySlug(String slug);
    long count();
    List<UUID> findRegisteredEventIds(List<UUID> eventIds, UUID userId);
}
