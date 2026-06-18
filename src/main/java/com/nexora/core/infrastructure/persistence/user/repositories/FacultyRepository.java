package com.nexora.core.infrastructure.persistence.user.repositories;

import com.nexora.core.infrastructure.persistence.user.entities.FacultyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacultyRepository extends JpaRepository<FacultyJpaEntity, UUID> {
    Optional<FacultyJpaEntity> findByNameIgnoreCase(String name);
    List<FacultyJpaEntity> findAllByOrderByNameAsc();
}
