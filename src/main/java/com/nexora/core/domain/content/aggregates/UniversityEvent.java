package com.nexora.core.domain.content.aggregates;

import com.nexora.core.domain.content.valueobjects.CommunityLinks;
import com.nexora.core.domain.content.valueobjects.Organizer;
import com.nexora.core.domain.shared.model.DomainModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityEvent extends DomainModel {
    private String slug;
    private String title;
    private String description;
    private OffsetDateTime date;
    private String location;
    private String category;
    private String image;
    private Organizer organizer;
    private CommunityLinks communityLinks;
    private List<UUID> attendeeIds;

    public static UniversityEvent create(String slug, String title, String description, OffsetDateTime date,
                                         String location, String category, String image,
                                         Organizer organizer, CommunityLinks communityLinks) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Slug cannot be empty");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        UniversityEvent event = new UniversityEvent();
        event.setSlug(slug.trim());
        event.setTitle(title.trim());
        event.setDescription(description);
        event.setDate(date);
        event.setLocation(location);
        event.setCategory(category);
        event.setImage(image);
        event.setOrganizer(organizer);
        event.setCommunityLinks(communityLinks);
        event.setAttendeeIds(new ArrayList<>());
        return event;
    }

    public void confirmRSVP(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (attendeeIds.stream().anyMatch(id -> id.equals(userId))) {
            throw new RuntimeException("Ya estás registrado en este evento");
        }
        attendeeIds.add(userId);
    }

    public boolean isUserRegistered(UUID userId) {
        return attendeeIds != null && attendeeIds.stream().anyMatch(id -> id.equals(userId));
    }

    public int getAttendeesCount() {
        return attendeeIds != null ? attendeeIds.size() : 0;
    }
}
