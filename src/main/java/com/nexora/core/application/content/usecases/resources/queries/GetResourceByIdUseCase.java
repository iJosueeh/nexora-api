package com.nexora.core.application.content.usecases.resources.queries;

import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetResourceByIdUseCase {

    private final AcademicResourceRepository academicResourceRepository;

    public Optional<AcademicResource> execute(UUID id) {
        return academicResourceRepository.findByIdNotDeleted(id);
    }
}
