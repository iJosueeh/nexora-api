package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.ResourceCategory;
import com.nexora.core.infrastructure.persistence.content.entities.ResourceCategoryJpaEntity;

public class ResourceCategoryMapper {
    public static ResourceCategory toDomain(ResourceCategoryJpaEntity entity) {
        return ResourceCategory.builder()
                .id(entity.getId())
                .careerId(entity.getCarrera().getId())
                .name(entity.getName())
                .build();
    }
}