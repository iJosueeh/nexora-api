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
public class CreateResourceCategoryUseCase {

    private final ResourceCategoryRepository resourceCategoryRepository;

    public ResourceCategory execute(String name, UUID careerId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be blank");
        }
        if (careerId == null) {
            throw new IllegalArgumentException("Career ID cannot be null");
        }
        if (resourceCategoryRepository.existsByCareerIdAndName(careerId, name.trim())) {
            throw new IllegalArgumentException("Category already exists for this career: " + name);
        }

        ResourceCategory category = ResourceCategory.builder()
                .name(name.trim())
                .careerId(careerId)
                .build();

        return resourceCategoryRepository.save(category);
    }
}
