package com.nexora.core.application.content.usecases.events.queries;

import com.nexora.core.domain.content.aggregates.UniversityEvent;
import com.nexora.core.domain.content.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetEventsUseCase {

    private final EventRepository eventRepository;

    public List<UniversityEvent> findAll(int limit, int offset, String category) {
        return eventRepository.findAll(limit, offset, category);
    }

    public Optional<UniversityEvent> findBySlug(String slug) {
        return eventRepository.findBySlug(slug);
    }

    public Optional<UniversityEvent> findById(UUID id) {
        return eventRepository.findById(id);
    }
}
