package com.nexora.core.application.content.services;

import com.nexora.core.domain.content.aggregates.UniversityEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UniversityEventService {
    List<UniversityEvent> findAll(int limit, int offset, String category);
    Optional<UniversityEvent> findBySlug(String slug);
    UniversityEvent confirmRSVP(UUID eventId, UUID userId);
    boolean isUserRegistered(UUID eventId, UUID userId);
    Map<UUID, Boolean> isUserRegisteredBatch(List<UUID> eventIds, UUID userId);
}
