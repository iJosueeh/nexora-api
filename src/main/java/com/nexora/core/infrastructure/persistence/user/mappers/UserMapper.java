package com.nexora.core.infrastructure.persistence.user.mappers;

import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.valueobjects.Email;
import com.nexora.core.domain.user.valueobjects.UserRole;
import com.nexora.core.domain.user.valueobjects.SupabaseId;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;

        UserRole role = null;
        if (entity.getRole() != null && entity.getRole().getName() != null) {
            role = UserRole.valueOf(entity.getRole().getName());
        }

        return User.builder()
                .id(entity.getId())
                .email(new Email(entity.getEmail()))
                .role(role)
                .isActive(entity.getIsActive())
                .supabaseId(entity.getSupabaseId() != null ? new SupabaseId(entity.getSupabaseId()) : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserJpaEntity toJpa(User domain) {
        if (domain == null) return null;

        UserJpaEntity entity = UserJpaEntity.builder()
                .email(domain.getEmail().value())
                .isActive(domain.getIsActive())
                .supabaseId(domain.getSupabaseId() != null ? domain.getSupabaseId().value() : null)
                .build();
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
