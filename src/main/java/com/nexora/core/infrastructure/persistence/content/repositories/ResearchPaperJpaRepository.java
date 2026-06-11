package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.ResearchPaperJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResearchPaperJpaRepository extends JpaRepository<ResearchPaperJpaEntity, UUID> {
    Optional<ResearchPaperJpaEntity> findBySlug(String slug);
    Page<ResearchPaperJpaEntity> findByFacultyIgnoreCase(String faculty, Pageable pageable);
}
