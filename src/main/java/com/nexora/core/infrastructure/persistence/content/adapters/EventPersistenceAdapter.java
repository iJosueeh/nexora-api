package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.UniversityEvent;
import com.nexora.core.domain.content.repositories.EventRepository;
import com.nexora.core.infrastructure.persistence.content.entities.UniversityEventJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.UniversityEventMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.UniversityEventJpaRepository;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import com.nexora.core.infrastructure.persistence.user.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventPersistenceAdapter implements EventRepository {

    private final UniversityEventJpaRepository eventJpaRepository;
    private final UniversityEventMapper eventMapper;
    private final UserJpaRepository userJpaRepository;

    @Override
    public List<UniversityEvent> findAll(int limit, int offset, String category) {
        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("date").descending());

        if (category != null && !category.equalsIgnoreCase("Todos")) {
            return eventJpaRepository.findByCategoryIgnoreCase(category, pageRequest).stream()
                    .map(eventMapper::toDomain)
                    .toList();
        }

        return eventJpaRepository.findAll(pageRequest).stream()
                .map(eventMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UniversityEvent> findBySlug(String slug) {
        return eventJpaRepository.findBySlug(slug).map(eventMapper::toDomain);
    }

    @Override
    public Optional<UniversityEvent> findById(UUID id) {
        return eventJpaRepository.findById(id).map(eventMapper::toDomain);
    }

    @Override
    @Transactional
    public UniversityEvent save(UniversityEvent event) {
        Set<UserJpaEntity> attendeeEntities = new HashSet<>();
        if (event.getAttendeeIds() != null && !event.getAttendeeIds().isEmpty()) {
            attendeeEntities.addAll(userJpaRepository.findAllById(event.getAttendeeIds()));
        }
        UniversityEventJpaEntity entity = eventMapper.toJpa(event, attendeeEntities);
        UniversityEventJpaEntity saved = eventJpaRepository.save(entity);
        return eventMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        eventJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return eventJpaRepository.findBySlug(slug).isPresent();
    }

    @Override
    public long count() {
        return eventJpaRepository.count();
    }

    @Override
    public List<UUID> findRegisteredEventIds(List<UUID> eventIds, UUID userId) {
        return eventJpaRepository.findRegisteredEventIds(eventIds, userId);
    }
}
