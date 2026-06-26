package com.nexora.core.application.content.usecases.resources.commands;

import com.nexora.core.domain.content.aggregates.ResourceCategory;
import com.nexora.core.domain.content.ports.ResourceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateResourceCategoryUseCase {

    private final ResourceCategoryRepository resourceCategoryRepository;

    public ResourceCategory execute(UUID id, String name, UUID careerId) {
        ResourceCategory existing = resourceCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource category not found"));

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }

        UUID targetCareerId = careerId != null ? careerId : existing.getCareerId();

        if (resourceCategoryRepository.existsByCareerIdAndNameAndIdNot(targetCareerId, name.trim(), id)) {
            throw new IllegalArgumentException("Category already exists for this career: " + name);
        }

        ResourceCategory updated = ResourceCategory.builder()
                .id(existing.getId())
                .name(name.trim())
                .careerId(targetCareerId)
                .build();

        return resourceCategoryRepository.save(updated);
    }
}
