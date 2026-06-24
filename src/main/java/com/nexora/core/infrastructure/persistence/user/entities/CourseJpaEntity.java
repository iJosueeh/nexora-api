package com.nexora.core.infrastructure.persistence.user.entities;

import com.nexora.core.infrastructure.persistence.common.entities.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "carreras")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseJpaEntity extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facultad_id", nullable = false)
    private FacultyJpaEntity facultad;

    @Column(name = "name", nullable = false)
    private String name;
}
