package com.nexora.core.infrastructure.persistence.content.repositories;
import com.nexora.core.infrastructure.persistence.content.entities.AcademicResourceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AcademicResourceJpaRepository extends JpaRepository<AcademicResourceJpaEntity, UUID> { }
