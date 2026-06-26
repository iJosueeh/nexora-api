package com.nexora.core.application.content.usecases.resources.queries;

import com.nexora.core.domain.content.aggregates.ResourceCategory;
import com.nexora.core.domain.content.ports.ResourceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetResourceCategoriesUseCase {

    private final ResourceCategoryRepository resourceCategoryRepository;

    public List<ResourceCategory> execute(UUID careerId) {
        if (careerId != null) {
            return resourceCategoryRepository.findAllByCareerId(careerId);
        }
        return resourceCategoryRepository.findAll();
    }
}
