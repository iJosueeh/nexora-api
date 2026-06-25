package com.nexora.core.infrastructure.persistence.content.repositories;
import com.nexora.core.infrastructure.persistence.content.entities.ResourceRatingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ResourceRatingJpaRepository extends JpaRepository<ResourceRatingJpaEntity, UUID> { }