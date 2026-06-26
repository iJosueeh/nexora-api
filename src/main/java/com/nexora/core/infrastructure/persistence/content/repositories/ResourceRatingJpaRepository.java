package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.ResourceRatingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface ResourceRatingJpaRepository extends JpaRepository<ResourceRatingJpaEntity, UUID> {
    Optional<ResourceRatingJpaEntity> findByUserIdAndResourceId(UUID userId, UUID resourceId);
    void deleteByUserIdAndResourceId(UUID userId, UUID resourceId);

    @Query("SELECT COUNT(r) FROM ResourceRatingJpaEntity r WHERE r.resource.id = :resourceId")
    int countByResourceId(@Param("resourceId") UUID resourceId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM ResourceRatingJpaEntity r WHERE r.resource.id = :resourceId")
    double averageRatingByResourceId(@Param("resourceId") UUID resourceId);
}