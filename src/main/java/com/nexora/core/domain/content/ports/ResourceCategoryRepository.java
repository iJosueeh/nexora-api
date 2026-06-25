package com.nexora.core.domain.content.ports;

import com.nexora.core.domain.content.aggregates.ResourceCategory;
import java.util.Optional;
import java.util.UUID;

public interface ResourceCategoryRepository {
    ResourceCategory save(ResourceCategory category);
    Optional<ResourceCategory> findById(UUID id);
}