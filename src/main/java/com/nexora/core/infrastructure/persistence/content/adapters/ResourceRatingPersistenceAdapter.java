package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.ResourceRating;
import com.nexora.core.domain.content.ports.ResourceRatingRepository;
import com.nexora.core.infrastructure.persistence.content.entities.ResourceRatingJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.ResourceRatingMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.ResourceRatingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResourceRatingPersistenceAdapter implements ResourceRatingRepository {

    private final ResourceRatingJpaRepository repository;
   
    @SuppressWarnings("null")
    @Override
    public ResourceRating save(ResourceRating rating) {
        ResourceRatingJpaEntity entity = ResourceRatingJpaEntity.builder()
                .userId(rating.getUserId())
                .rating(rating.getRating())
                .build();
                
        // Asignamos el ID heredado usando el setter
        if (rating.getId() != null) {
            entity.setId(rating.getId());
        }
        
        return ResourceRatingMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<ResourceRating> findByUserIdAndResourceId(UUID userId, UUID resourceId) {
        return Optional.empty(); // Stub provisional para compilar
    }
}