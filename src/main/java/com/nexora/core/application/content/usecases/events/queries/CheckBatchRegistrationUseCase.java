package com.nexora.core.application.content.usecases.events.queries;

import com.nexora.core.domain.content.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckBatchRegistrationUseCase {

    private final EventRepository eventRepository;

    public Map<UUID, Boolean> execute(List<UUID> eventIds, UUID userId) {
        List<UUID> registeredIds = eventRepository.findRegisteredEventIds(eventIds, userId);
        return eventIds.stream().collect(Collectors.toMap(
            id -> id,
            registeredIds::contains
        ));
    }
}
