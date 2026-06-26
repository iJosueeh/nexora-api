package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.ResourceCategory;
import com.nexora.core.infrastructure.persistence.content.entities.ResourceCategoryJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.CourseJpaEntity;

public class ResourceCategoryMapper {
    public static ResourceCategory toDomain(ResourceCategoryJpaEntity entity) {
        return ResourceCategory.builder()
                .id(entity.getId())
                .careerId(entity.getCarrera().getId())
                .name(entity.getName())
                .build();
    }

    public static ResourceCategoryJpaEntity toJpa(ResourceCategory domain, CourseJpaEntity carrera) {
        ResourceCategoryJpaEntity entity = ResourceCategoryJpaEntity.builder()
                .name(domain.getName())
                .carrera(carrera)
                .build();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        return entity;
    }
}