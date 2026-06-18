package com.nexora.core.application.content.usecases.events.commands;

import com.nexora.core.domain.content.aggregates.UniversityEvent;
import com.nexora.core.domain.content.repositories.EventRepository;
import com.nexora.core.domain.content.valueobjects.CommunityLinks;
import com.nexora.core.domain.content.valueobjects.Organizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EditEventUseCase {

    private final EventRepository eventRepository;

    public UniversityEvent execute(UUID eventId, String title, String description,
                                    String date, String location, String category,
                                    String image, String organizerName, String organizerRole,
                                    String whatsapp, String telegram, String discord) {
        UniversityEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (title != null && !title.isBlank()) {
            event.setTitle(title.trim());
        }
        if (description != null) {
            event.setDescription(description);
        }
        if (date != null && !date.isBlank()) {
            event.setDate(date);
        }
        if (location != null) {
            event.setLocation(location);
        }
        if (category != null) {
            event.setCategory(category);
        }
        if (image != null) {
            event.setImage(image);
        }
        if (organizerName != null && !organizerName.isBlank()) {
            event.setOrganizer(Organizer.of(organizerName, organizerRole));
        }
        if (whatsapp != null || telegram != null || discord != null) {
            CommunityLinks existing = event.getCommunityLinks();
            event.setCommunityLinks(CommunityLinks.of(
                    whatsapp != null ? whatsapp : (existing != null ? existing.getWhatsapp() : null),
                    telegram != null ? telegram : (existing != null ? existing.getTelegram() : null),
                    discord != null ? discord : (existing != null ? existing.getDiscord() : null)));
        }

        return eventRepository.save(event);
    }
}
