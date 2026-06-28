package com.nexora.core.application.content.usecases.resources.commands;

import com.nexora.core.domain.content.ports.ResourceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteResourceCategoryUseCase {

    private final ResourceCategoryRepository resourceCategoryRepository;

    public boolean execute(UUID id) {
        if (!resourceCategoryRepository.findById(id).isPresent()) {
            throw new RuntimeException("Resource category not found");
        }
        resourceCategoryRepository.deleteById(id);
        return true;
    }
}
