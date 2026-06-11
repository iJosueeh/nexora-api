package com.nexora.core.infrastructure.persistence.user.repositories;

import com.nexora.core.infrastructure.persistence.user.entities.AcademicInterestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicInterestRepository extends JpaRepository<AcademicInterestJpaEntity, UUID> {
    Optional<AcademicInterestJpaEntity> findByName(String name);
    List<AcademicInterestJpaEntity> findAllByOrderByNameAsc();
}
