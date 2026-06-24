package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.enums.GroupMembershipStatus;
import com.nexora.core.domain.content.enums.GroupRole;
import com.nexora.core.infrastructure.persistence.content.entities.GroupMembershipJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class GroupMembershipMapper {

    public GroupMembership toDomain(GroupMembershipJpaEntity entity) {
        if (entity == null) return null;

        return GroupMembership.builder()
                .id(entity.getId())
                .groupId(entity.getGroup() != null ? entity.getGroup().getId() : null)
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .role(parseRole(entity.getRole()))
                .status(parseStatus(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public GroupMembershipJpaEntity toJpa(GroupMembership domain) {
        if (domain == null) return null;

        GroupMembershipJpaEntity entity = new GroupMembershipJpaEntity();
        entity.setId(domain.getId());
        entity.setRole(domain.getRole() != null ? domain.getRole().name() : "MEMBER");
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : "APPROVED");
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private GroupRole parseRole(String role) {
        if (role == null) return GroupRole.MEMBER;
        try {
            return GroupRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            return GroupRole.MEMBER;
        }
    }

    private GroupMembershipStatus parseStatus(String status) {
        if (status == null) return GroupMembershipStatus.APPROVED;
        try {
            return GroupMembershipStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return GroupMembershipStatus.APPROVED;
        }
    }
}
