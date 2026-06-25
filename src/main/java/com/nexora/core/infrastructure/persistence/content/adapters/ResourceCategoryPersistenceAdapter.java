package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.ResourceCategory;
import com.nexora.core.domain.content.ports.ResourceCategoryRepository;
import com.nexora.core.infrastructure.persistence.content.entities.ResourceCategoryJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.ResourceCategoryMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.ResourceCategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResourceCategoryPersistenceAdapter implements ResourceCategoryRepository {

    private final ResourceCategoryJpaRepository repository;
   
    @SuppressWarnings("null")
    @Override
    public ResourceCategory save(ResourceCategory category) {
        ResourceCategoryJpaEntity entity = ResourceCategoryJpaEntity.builder()
                .name(category.getName())
                .build();
                
        // Asignamos el ID heredado usando el setter
        if (category.getId() != null) {
            entity.setId(category.getId());
        }
        
        return ResourceCategoryMapper.toDomain(repository.save(entity));
    }

    @SuppressWarnings("null")
    @Override
    public Optional<ResourceCategory> findById(UUID id) {
        return repository.findById(id).map(ResourceCategoryMapper::toDomain);
    }
}