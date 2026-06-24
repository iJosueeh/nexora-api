package com.nexora.core.application.content.usecases.events.queries;

import com.nexora.core.domain.content.aggregates.UniversityEvent;
import com.nexora.core.domain.content.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchEventsUseCase {

    private final EventRepository eventRepository;

    public List<UniversityEvent> execute(String query, int limit, int offset) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return eventRepository.searchByFullText(query.trim(), limit, offset);
    }
}
