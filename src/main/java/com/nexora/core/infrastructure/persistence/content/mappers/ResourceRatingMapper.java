package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.ResourceRating;
import com.nexora.core.infrastructure.persistence.content.entities.ResourceRatingJpaEntity;

public class ResourceRatingMapper {
    public static ResourceRating toDomain(ResourceRatingJpaEntity entity) {
        return ResourceRating.builder()
                .id(entity.getId())
                .resourceId(entity.getResource().getId())
                .userId(entity.getUserId())
                .rating(entity.getRating())
                .build();
    }
}