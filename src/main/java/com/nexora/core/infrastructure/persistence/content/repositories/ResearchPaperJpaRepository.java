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
    long countByFacultyIgnoreCase(String faculty);

    @org.springframework.data.jpa.repository.Query(value = "SELECT p.* FROM research_papers p WHERE p.search_vector @@ plainto_tsquery('spanish', :query) ORDER BY ts_rank(p.search_vector, plainto_tsquery('spanish', :query)) DESC",
           countQuery = "SELECT COUNT(*) FROM research_papers p WHERE p.search_vector @@ plainto_tsquery('spanish', :query)",
           nativeQuery = true)
    java.util.List<ResearchPaperJpaEntity> searchByFullText(@org.springframework.data.repository.query.Param("query") String query, Pageable pageable);
}
