package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.PostJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostJpaRepository extends JpaRepository<PostJpaEntity, UUID> {

    List<PostJpaEntity> findAllByOrderByCreatedAtDesc();

    Page<PostJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = "SELECT p.* FROM posts p WHERE p.search_vector @@ plainto_tsquery('spanish', :query) ORDER BY ts_rank(p.search_vector, plainto_tsquery('spanish', :query)) DESC",
           countQuery = "SELECT COUNT(*) FROM posts p WHERE p.search_vector @@ plainto_tsquery('spanish', :query)",
           nativeQuery = true)
    List<PostJpaEntity> searchByFullText(@Param("query") String query, Pageable pageable);
}
