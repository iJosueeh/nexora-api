package com.nexora.core.application.content.usecases.events.commands;

import com.nexora.core.domain.content.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteEventUseCase {

    private final EventRepository eventRepository;

    public boolean execute(UUID eventId) {
        if (!eventRepository.findById(eventId).isPresent()) {
            throw new IllegalArgumentException("Evento no encontrado");
        }
        eventRepository.deleteById(eventId);
        return true;
    }
}
