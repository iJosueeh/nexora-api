package com.nexora.core.infrastructure.persistence.content.entities;

import com.nexora.core.infrastructure.persistence.common.entities.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "academic_resources")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicResourceJpaEntity extends AuditableJpaEntity {

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String type; // Enum convertido a String

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ResourceCategoryJpaEntity category;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_format", nullable = false)
    private String fileFormat;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer ratingsCount = 0;

    @Builder.Default
    private Integer downloadCount = 0;

    private OffsetDateTime deletedAt;
}
