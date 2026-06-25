package com.nexora.core.domain.content.ports;

import com.nexora.core.domain.content.aggregates.AcademicResource;
import java.util.Optional;
import java.util.UUID;

public interface AcademicResourceRepository {
    AcademicResource save(AcademicResource resource);
    Optional<AcademicResource> findById(UUID id);
}