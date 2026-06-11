package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.PostJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostJpaRepository extends JpaRepository<PostJpaEntity, UUID> {

    List<PostJpaEntity> findAllByOrderByCreatedAtDesc();

    Page<PostJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
