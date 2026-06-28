package com.nexora.core.infrastructure.persistence.content.entities;
import com.nexora.core.infrastructure.persistence.common.entities.BaseJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.CourseJpaEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "resource_categories")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCategoryJpaEntity extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrera_id", nullable = false)
    private CourseJpaEntity carrera;

    @Column(name = "name", nullable = false)
    private String name;
}