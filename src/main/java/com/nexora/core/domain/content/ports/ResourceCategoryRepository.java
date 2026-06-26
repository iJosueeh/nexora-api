package com.nexora.core.domain.content.ports;

import com.nexora.core.domain.content.aggregates.ResourceCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceCategoryRepository {
    ResourceCategory save(ResourceCategory category);
    Optional<ResourceCategory> findById(UUID id);
    List<ResourceCategory> findAll();
    List<ResourceCategory> findAllByCareerId(UUID careerId);
    void deleteById(UUID id);
    boolean existsByCareerIdAndName(UUID careerId, String name);
    boolean existsByCareerIdAndNameAndIdNot(UUID careerId, String name, UUID id);
}