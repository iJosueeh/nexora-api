package com.nexora.core.application.content.services.impl;

import com.nexora.core.application.content.services.UniversityEventService;
import com.nexora.core.domain.content.aggregates.UniversityEvent;
import com.nexora.core.domain.content.repositories.EventRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UniversityEventServiceImpl implements UniversityEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<UniversityEvent> findAll(int limit, int offset, String category) {
        return eventRepository.findAll(limit, offset, category);
    }

    @Override
    public Optional<UniversityEvent> findBySlug(String slug) {
        return eventRepository.findBySlug(slug);
    }

    @Override
    @Transactional
    public UniversityEvent confirmRSVP(UUID eventId, UUID userId) {
        UniversityEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        event.confirmRSVP(userId);
        return eventRepository.save(event);
    }

    @Override
    public boolean isUserRegistered(UUID eventId, UUID userId) {
        return eventRepository.findById(eventId)
                .map(event -> event.isUserRegistered(userId))
                .orElse(false);
    }

    @Override
    public Map<UUID, Boolean> isUserRegisteredBatch(List<UUID> eventIds, UUID userId) {
        if (eventIds == null || eventIds.isEmpty() || userId == null) {
            return Map.of();
        }

        List<UUID> registeredIds = eventRepository.findRegisteredEventIds(eventIds, userId);
        return eventIds.stream().collect(Collectors.toMap(
            id -> id,
            registeredIds::contains
        ));
    }
}
