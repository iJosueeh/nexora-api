package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.GroupInvitation;
import com.nexora.core.domain.content.enums.GroupInvitationStatus;
import com.nexora.core.infrastructure.persistence.content.entities.GroupInvitationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class GroupInvitationMapper {

    public GroupInvitation toDomain(GroupInvitationJpaEntity entity) {
        if (entity == null) return null;

        return GroupInvitation.builder()
                .id(entity.getId())
                .groupId(entity.getGroup() != null ? entity.getGroup().getId() : null)
                .inviterId(entity.getInviter() != null ? entity.getInviter().getId() : null)
                .invitedUserId(entity.getInvitedUser() != null ? entity.getInvitedUser().getId() : null)
                .status(parseStatus(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public GroupInvitationJpaEntity toJpa(GroupInvitation domain) {
        if (domain == null) return null;

        GroupInvitationJpaEntity entity = new GroupInvitationJpaEntity();
        entity.setId(domain.getId());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : "PENDING");
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private GroupInvitationStatus parseStatus(String status) {
        if (status == null) return GroupInvitationStatus.PENDING;
        try {
            return GroupInvitationStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return GroupInvitationStatus.PENDING;
        }
    }
}
