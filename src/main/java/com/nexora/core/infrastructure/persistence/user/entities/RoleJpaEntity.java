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
@Table(name = "roles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleJpaEntity extends BaseJpaEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
