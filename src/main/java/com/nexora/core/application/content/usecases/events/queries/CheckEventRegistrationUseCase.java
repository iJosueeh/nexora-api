package com.nexora.core.application.content.usecases.events.queries;

import com.nexora.core.domain.content.aggregates.UniversityEvent;
import com.nexora.core.domain.content.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckEventRegistrationUseCase {

    private final EventRepository eventRepository;

    public boolean execute(UUID eventId, UUID userId) {
        Optional<UniversityEvent> eventOpt = eventRepository.findById(eventId);
        return eventOpt.map(event -> event.isUserRegistered(userId)).orElse(false);
    }
}
