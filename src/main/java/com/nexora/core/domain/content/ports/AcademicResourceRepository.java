package com.nexora.core.domain.content.ports;

import com.nexora.core.domain.content.aggregates.AcademicResource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcademicResourceRepository {
    AcademicResource save(AcademicResource resource);
    Optional<AcademicResource> findById(UUID id);
    Optional<AcademicResource> findByIdNotDeleted(UUID id);
    List<AcademicResource> findAll(UUID careerId, UUID categoryId, String type, UUID authorId, Double minRating, int limit, int offset);
    List<AcademicResource> findAllByIds(List<UUID> ids);
}