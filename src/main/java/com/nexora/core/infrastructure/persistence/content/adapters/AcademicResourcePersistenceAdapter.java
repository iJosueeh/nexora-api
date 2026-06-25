package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import com.nexora.core.infrastructure.persistence.content.entities.AcademicResourceJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.AcademicResourceMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.AcademicResourceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AcademicResourcePersistenceAdapter implements AcademicResourceRepository {

    private final AcademicResourceJpaRepository repository;

    @SuppressWarnings("null")
    @Override
    public AcademicResource save(AcademicResource resource) {
        AcademicResourceJpaEntity entity = AcademicResourceJpaEntity.builder()
                .slug(resource.getSlug())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .type(resource.getType())
                .authorId(resource.getAuthorId())
                .fileUrl(resource.getFileUrl())
                .fileSize(resource.getFileSize())
                .fileFormat(resource.getFileFormat())
                .averageRating(resource.getAverageRating() != null ? resource.getAverageRating() : 0.0)
                .ratingsCount(resource.getRatingsCount() != null ? resource.getRatingsCount() : 0)
                .downloadCount(resource.getDownloadCount() != null ? resource.getDownloadCount() : 0)
                .build();
        
        // Asignamos el ID heredado usando el setter
        if (resource.getId() != null) {
            entity.setId(resource.getId());
        }
        
        return AcademicResourceMapper.toDomain(repository.save(entity));
    }
   
    @SuppressWarnings("null")
    @Override
    public Optional<AcademicResource> findById(UUID id) {
        return repository.findById(id).map(AcademicResourceMapper::toDomain);
    }
}