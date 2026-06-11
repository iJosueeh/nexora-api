package com.nexora.core.infrastructure.persistence.user.repositories;

import com.nexora.core.infrastructure.persistence.user.entities.CourseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<CourseJpaEntity, UUID> {
    Optional<CourseJpaEntity> findByNameIgnoreCase(String name);
    List<CourseJpaEntity> findAllByOrderByNameAsc();
}
