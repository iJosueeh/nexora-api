package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.AcademicResourceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcademicResourceJpaRepository extends JpaRepository<AcademicResourceJpaEntity, UUID> {

    Optional<AcademicResourceJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    @Query("SELECT r FROM AcademicResourceJpaEntity r " +
           "LEFT JOIN FETCH r.category c " +
           "WHERE r.deletedAt IS NULL " +
           "AND (:careerId IS NULL OR c.carrera.id = :careerId) " +
           "AND (:categoryId IS NULL OR r.category.id = :categoryId) " +
           "AND (:type IS NULL OR r.type = :type) " +
           "AND (:authorId IS NULL OR r.authorId = :authorId) " +
           "AND (:minRating IS NULL OR r.averageRating >= :minRating) " +
           "ORDER BY r.averageRating DESC, r.createdAt DESC")
    List<AcademicResourceJpaEntity> findAllFiltered(
            @Param("careerId") UUID careerId,
            @Param("categoryId") UUID categoryId,
            @Param("type") String type,
            @Param("authorId") UUID authorId,
            @Param("minRating") Double minRating);

    List<AcademicResourceJpaEntity> findByIdIn(List<UUID> ids);
}
