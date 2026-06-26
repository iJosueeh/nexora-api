package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.ResourceCategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ResourceCategoryJpaRepository extends JpaRepository<ResourceCategoryJpaEntity, UUID> {
    List<ResourceCategoryJpaEntity> findAllByCarreraIdOrderByNameAsc(UUID carreraId);
    boolean existsByCarreraIdAndNameIgnoreCase(UUID carreraId, String name);
    boolean existsByCarreraIdAndNameIgnoreCaseAndIdNot(UUID carreraId, String name, UUID id);
}
