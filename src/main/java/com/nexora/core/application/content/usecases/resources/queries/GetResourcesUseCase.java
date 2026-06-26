package com.nexora.core.application.content.usecases.resources.queries;

import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetResourcesUseCase {

    private final AcademicResourceRepository academicResourceRepository;

    public List<AcademicResource> execute(UUID careerId, UUID categoryId, String type, UUID authorId, Double minRating, int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int safeOffset = Math.max(0, offset);
        return academicResourceRepository.findAll(careerId, categoryId, type, authorId, minRating, safeLimit, safeOffset);
    }
}
