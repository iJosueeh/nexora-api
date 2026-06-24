package com.nexora.core.infrastructure.persistence.user.repositories;

import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<UserJpaEntity> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
