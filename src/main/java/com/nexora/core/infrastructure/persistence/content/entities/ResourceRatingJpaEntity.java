package com.nexora.core.infrastructure.persistence.content.entities;

import com.nexora.core.infrastructure.persistence.common.entities.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "resource_ratings", uniqueConstraints = 
    @UniqueConstraint(columnNames = {"user_id", "resource_id"}))
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRatingJpaEntity extends AuditableJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private AcademicResourceJpaEntity resource;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Integer rating;
}