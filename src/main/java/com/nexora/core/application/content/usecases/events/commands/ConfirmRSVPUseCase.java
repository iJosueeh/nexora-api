package com.nexora.core.application.content.usecases.events.commands;

import com.nexora.core.domain.content.aggregates.UniversityEvent;
import com.nexora.core.domain.content.repositories.EventRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfirmRSVPUseCase {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public UniversityEvent execute(UUID eventId, UUID userId) {
        UniversityEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        event.confirmRSVP(userId);
        return eventRepository.save(event);
    }
}
