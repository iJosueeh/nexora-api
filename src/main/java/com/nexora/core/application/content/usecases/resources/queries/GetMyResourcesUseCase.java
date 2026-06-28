package com.nexora.core.application.content.usecases.resources.queries;

import com.nexora.core.application.security.services.SecurityService;
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
public class GetMyResourcesUseCase {

    private final AcademicResourceRepository academicResourceRepository;
    private final SecurityService securityService;

    public List<AcademicResource> execute(int limit, int offset) {
        UUID currentUserId = securityService.getCurrentUserId();
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int safeOffset = Math.max(0, offset);
        return academicResourceRepository.findAllByAuthorId(currentUserId, safeLimit, safeOffset);
    }
}
