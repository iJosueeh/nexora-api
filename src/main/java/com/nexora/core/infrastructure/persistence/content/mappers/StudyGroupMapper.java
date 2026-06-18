package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.infrastructure.persistence.content.entities.StudyGroupJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class StudyGroupMapper {

    public StudyGroup toDomain(StudyGroupJpaEntity entity) {
        if (entity == null) return null;

        UUID authorId = entity.getAuthor() != null ? entity.getAuthor().getId() : null;

        return StudyGroup.builder()
                .id(entity.getId())
                .slug(entity.getSlug())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .isPrivate(Boolean.TRUE.equals(entity.getIsPrivate()))
                .maxMembers(entity.getMaxMembers() != null ? entity.getMaxMembers() : 50)
                .authorId(authorId)
                .memberIds(new ArrayList<>())
                .memberships(new ArrayList<>())
                .currentUserIsMember(false)
                .currentUserRole(null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public StudyGroupJpaEntity toJpa(StudyGroup domain, UserJpaEntity author) {
        if (domain == null) return null;

        StudyGroupJpaEntity entity = StudyGroupJpaEntity.builder()
                .slug(domain.getSlug())
                .name(domain.getName())
                .description(domain.getDescription())
                .category(domain.getCategory())
                .isPrivate(domain.isPrivate())
                .maxMembers(domain.getMaxMembers())
                .author(author)
                .build();
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
