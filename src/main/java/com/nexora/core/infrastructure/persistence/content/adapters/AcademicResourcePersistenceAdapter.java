package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import com.nexora.core.infrastructure.persistence.content.entities.AcademicResourceJpaEntity;
import com.nexora.core.infrastructure.persistence.content.entities.ResourceCategoryJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.AcademicResourceMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.AcademicResourceJpaRepository;
import com.nexora.core.infrastructure.persistence.content.repositories.ResourceCategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AcademicResourcePersistenceAdapter implements AcademicResourceRepository {

    private final AcademicResourceJpaRepository repository;
    private final ResourceCategoryJpaRepository categoryRepository;

    @SuppressWarnings("null")
    @Override
    public AcademicResource save(AcademicResource resource) {
        ResourceCategoryJpaEntity category = categoryRepository.findById(resource.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + resource.getCategoryId()));

        AcademicResourceJpaEntity entity = AcademicResourceJpaEntity.builder()
                .slug(resource.getSlug())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .type(resource.getType())
                .category(category)
                .authorId(resource.getAuthorId())
                .fileUrl(resource.getFileUrl())
                .fileSize(resource.getFileSize())
                .fileFormat(resource.getFileFormat())
                .averageRating(resource.getAverageRating() != null ? resource.getAverageRating() : 0.0)
                .ratingsCount(resource.getRatingsCount() != null ? resource.getRatingsCount() : 0)
                .downloadCount(resource.getDownloadCount() != null ? resource.getDownloadCount() : 0)
                .build();
        
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

    @Override
    public Optional<AcademicResource> findByIdNotDeleted(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(AcademicResourceMapper::toDomain);
    }

    @Override
    public List<AcademicResource> findAll(UUID careerId, UUID categoryId, String type, UUID authorId, Double minRating, int limit, int offset) {
        return repository.findAllFiltered(careerId, categoryId, type, authorId, minRating)
                .stream()
                .skip(offset)
                .limit(limit)
                .map(AcademicResourceMapper::toDomain)
                .toList();
    }

    @Override
    public List<AcademicResource> findAllByIds(List<UUID> ids) {
        if (ids.isEmpty()) return List.of();
        return repository.findByIdIn(ids).stream()
                .map(AcademicResourceMapper::toDomain)
                .toList();
    }

    @Override
    public List<AcademicResource> findAllByAuthorId(UUID authorId, int limit, int offset) {
        return repository.findAllByAuthorId(authorId)
                .stream()
                .skip(offset)
                .limit(limit)
                .map(AcademicResourceMapper::toDomain)
                .toList();
    }
}