package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.CommentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, UUID> {
    List<CommentJpaEntity> findByPostIdOrderByCreatedAtAsc(UUID postId);
    List<CommentJpaEntity> findByParentIdOrderByCreatedAtAsc(UUID parentId);
}
