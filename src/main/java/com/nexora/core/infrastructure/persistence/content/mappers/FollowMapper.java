package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.Follow;
import com.nexora.core.infrastructure.persistence.content.entities.FollowJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class FollowMapper {

    public Follow toDomain(FollowJpaEntity entity) {
        if (entity == null) return null;

        return Follow.builder()
                .id(entity.getId())
                .followerId(entity.getFollowerId())
                .followingId(entity.getFollowingId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public FollowJpaEntity toJpa(Follow domain) {
        if (domain == null) return null;

        FollowJpaEntity entity = FollowJpaEntity.builder()
                .followerId(domain.getFollowerId())
                .followingId(domain.getFollowingId())
                .build();
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
