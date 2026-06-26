package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.ResourceCategory;
import com.nexora.core.domain.content.ports.ResourceCategoryRepository;
import com.nexora.core.infrastructure.persistence.content.entities.ResourceCategoryJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.ResourceCategoryMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.ResourceCategoryJpaRepository;
import com.nexora.core.infrastructure.persistence.user.entities.CourseJpaEntity;
import com.nexora.core.infrastructure.persistence.user.repositories.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResourceCategoryPersistenceAdapter implements ResourceCategoryRepository {

    private final ResourceCategoryJpaRepository repository;
    private final CourseRepository courseRepository;

    @Override
    public ResourceCategory save(ResourceCategory category) {
        CourseJpaEntity carrera = courseRepository.findById(category.getCareerId())
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + category.getCareerId()));
        ResourceCategoryJpaEntity entity = ResourceCategoryMapper.toJpa(category, carrera);
        return ResourceCategoryMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<ResourceCategory> findById(UUID id) {
        return repository.findById(id).map(ResourceCategoryMapper::toDomain);
    }

    @Override
    public List<ResourceCategory> findAll() {
        return repository.findAll().stream()
                .map(ResourceCategoryMapper::toDomain)
                .toList();
    }

    @Override
    public List<ResourceCategory> findAllByCareerId(UUID careerId) {
        return repository.findAllByCarreraIdOrderByNameAsc(careerId).stream()
                .map(ResourceCategoryMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByCareerIdAndName(UUID careerId, String name) {
        return repository.existsByCarreraIdAndNameIgnoreCase(careerId, name);
    }

    @Override
    public List<ResourceCategory> findAllByIds(List<UUID> ids) {
        if (ids.isEmpty()) return List.of();
        return repository.findByIdIn(ids).stream()
                .map(ResourceCategoryMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCareerIdAndNameAndIdNot(UUID careerId, String name, UUID id) {
        return repository.existsByCarreraIdAndNameIgnoreCaseAndIdNot(careerId, name, id);
    }
}