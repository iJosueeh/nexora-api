package com.nexora.core.infrastructure.persistence.user.entities;

import com.nexora.core.infrastructure.persistence.common.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "intereses_academicos")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicInterestJpaEntity extends BaseJpaEntity {

    @Column(name = "name", unique = true, nullable = false)
    private String name;
}
