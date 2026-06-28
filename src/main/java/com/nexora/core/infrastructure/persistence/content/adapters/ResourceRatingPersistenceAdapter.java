package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.ResourceRating;
import com.nexora.core.domain.content.ports.ResourceRatingRepository;
import com.nexora.core.infrastructure.persistence.content.entities.AcademicResourceJpaEntity;
import com.nexora.core.infrastructure.persistence.content.entities.ResourceRatingJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.ResourceRatingMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.AcademicResourceJpaRepository;
import com.nexora.core.infrastructure.persistence.content.repositories.ResourceRatingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResourceRatingPersistenceAdapter implements ResourceRatingRepository {

    private final ResourceRatingJpaRepository repository;
    private final AcademicResourceJpaRepository resourceRepository;

    @Override
    public ResourceRating save(ResourceRating rating) {
        ResourceRatingJpaEntity entity = repository.findByUserIdAndResourceId(rating.getUserId(), rating.getResourceId())
                .orElseGet(() -> {
                    AcademicResourceJpaEntity resource = resourceRepository.findById(rating.getResourceId())
                            .orElseThrow(() -> new RuntimeException("Resource not found"));
                    return ResourceRatingJpaEntity.builder()
                            .resource(resource)
                            .userId(rating.getUserId())
                            .build();
                });

        entity.setRating(rating.getRating());

        if (rating.getId() != null) {
            entity.setId(rating.getId());
        }

        return ResourceRatingMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<ResourceRating> findByUserIdAndResourceId(UUID userId, UUID resourceId) {
        return repository.findByUserIdAndResourceId(userId, resourceId)
                .map(ResourceRatingMapper::toDomain);
    }

    @Override
    public void deleteByUserIdAndResourceId(UUID userId, UUID resourceId) {
        repository.deleteByUserIdAndResourceId(userId, resourceId);
    }

    @Override
    public int countByResourceId(UUID resourceId) {
        return repository.countByResourceId(resourceId);
    }

    @Override
    public double averageRatingByResourceId(UUID resourceId) {
        return repository.averageRatingByResourceId(resourceId);
    }
}