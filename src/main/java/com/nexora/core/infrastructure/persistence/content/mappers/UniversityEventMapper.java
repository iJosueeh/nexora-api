package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.UniversityEvent;
import com.nexora.core.domain.content.valueobjects.CommunityLinks;
import com.nexora.core.domain.content.valueobjects.Organizer;
import com.nexora.core.infrastructure.persistence.content.entities.UniversityEventJpaEntity;
import com.nexora.core.infrastructure.persistence.content.entities.UniversityEventJpaEntity.CommunityLinksJpaEmbeddable;
import com.nexora.core.infrastructure.persistence.content.entities.UniversityEventJpaEntity.OrganizerJpaEmbeddable;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UniversityEventMapper {

    public UniversityEvent toDomain(UniversityEventJpaEntity entity) {
        if (entity == null) return null;

        List<UUID> attendeeIds = entity.getAttendees() != null
                ? entity.getAttendees().stream().map(UserJpaEntity::getId).toList()
                : new ArrayList<>();

        return UniversityEvent.builder()
                .id(entity.getId())
                .slug(entity.getSlug())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .date(entity.getDate())
                .location(entity.getLocation())
                .category(entity.getCategory())
                .image(entity.getImage())
                .organizer(mapOrganizerToDomain(entity.getOrganizer()))
                .communityLinks(mapCommunityLinksToDomain(entity.getCommunityLinks()))
                .attendeeIds(attendeeIds)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UniversityEventJpaEntity toJpa(UniversityEvent domain, Set<UserJpaEntity> attendeeEntities) {
        if (domain == null) return null;

        UniversityEventJpaEntity entity = UniversityEventJpaEntity.builder()
                .slug(domain.getSlug())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .date(domain.getDate())
                .location(domain.getLocation())
                .category(domain.getCategory())
                .image(domain.getImage())
                .organizer(mapOrganizerToJpa(domain.getOrganizer()))
                .communityLinks(mapCommunityLinksToJpa(domain.getCommunityLinks()))
                .attendees(attendeeEntities)
                .build();
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private Organizer mapOrganizerToDomain(OrganizerJpaEmbeddable embeddable) {
        if (embeddable == null) return null;
        return Organizer.of(embeddable.getName(), embeddable.getRole());
    }

    private OrganizerJpaEmbeddable mapOrganizerToJpa(Organizer organizer) {
        if (organizer == null) return null;
        return OrganizerJpaEmbeddable.builder()
                .name(organizer.getName())
                .role(organizer.getRole())
                .build();
    }

    private CommunityLinks mapCommunityLinksToDomain(CommunityLinksJpaEmbeddable embeddable) {
        if (embeddable == null) return null;
        return CommunityLinks.of(embeddable.getWhatsapp(), embeddable.getTelegram(), embeddable.getDiscord());
    }

    private CommunityLinksJpaEmbeddable mapCommunityLinksToJpa(CommunityLinks links) {
        if (links == null) return null;
        return CommunityLinksJpaEmbeddable.builder()
                .whatsapp(links.getWhatsapp())
                .telegram(links.getTelegram())
                .discord(links.getDiscord())
                .build();
    }
}
