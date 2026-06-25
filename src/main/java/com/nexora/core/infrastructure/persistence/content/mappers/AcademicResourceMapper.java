package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.infrastructure.persistence.content.entities.AcademicResourceJpaEntity;

public class AcademicResourceMapper {

    public static AcademicResource toDomain(AcademicResourceJpaEntity entity) {
        return AcademicResource.builder()
                .id(entity.getId())
                .slug(entity.getSlug())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .type(entity.getType())
                .categoryId(entity.getCategory().getId())
                .authorId(entity.getAuthorId())
                .fileUrl(entity.getFileUrl())
                .fileSize(entity.getFileSize())
                .fileFormat(entity.getFileFormat())
                .averageRating(entity.getAverageRating())
                .ratingsCount(entity.getRatingsCount())
                .downloadCount(entity.getDownloadCount())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}