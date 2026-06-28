package com.nexora.core.application.content.usecases.events.commands;

import com.nexora.core.common.util.SlugUtils;
import com.nexora.core.domain.content.aggregates.UniversityEvent;
import com.nexora.core.domain.content.repositories.EventRepository;
import com.nexora.core.domain.content.valueobjects.CommunityLinks;
import com.nexora.core.domain.content.valueobjects.Organizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateEventUseCase {

    private final EventRepository eventRepository;

    public UniversityEvent execute(String title, String description, OffsetDateTime date,
                                    String location, String category, String image,
                                    String organizerName, String organizerRole,
                                    String whatsapp, String telegram, String discord) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El título del evento es obligatorio");
        }
        if (date == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria");
        }

        String slug = SlugUtils.makeSlug(title);
        if (eventRepository.existsBySlug(slug)) {
            slug = slug + "-" + java.util.UUID.randomUUID().toString().substring(0, 6);
        }

        Organizer organizer = (organizerName != null && !organizerName.isBlank())
                ? Organizer.of(organizerName, organizerRole)
                : null;

        CommunityLinks communityLinks = (whatsapp != null || telegram != null || discord != null)
                ? CommunityLinks.of(whatsapp, telegram, discord)
                : null;

        UniversityEvent event = UniversityEvent.create(
                slug, title, description != null && !description.isBlank() ? description : "Sin descripción", date, location, category, image,
                organizer, communityLinks);

        return eventRepository.save(event);
    }
}
