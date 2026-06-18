package com.nexora.core.domain.content.aggregates;

import com.nexora.core.domain.content.enums.GroupMembershipStatus;
import com.nexora.core.domain.content.enums.GroupRole;
import com.nexora.core.domain.shared.model.DomainModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMembership extends DomainModel {
    private UUID groupId;
    private UUID userId;
    private GroupRole role;
    private GroupMembershipStatus status;

    public static GroupMembership create(UUID groupId, UUID userId, GroupRole role, GroupMembershipStatus status) {
        GroupMembership membership = new GroupMembership();
        membership.setGroupId(groupId);
        membership.setUserId(userId);
        membership.setRole(role);
        membership.setStatus(status);
        return membership;
    }

    public boolean isApproved() {
        return GroupMembershipStatus.APPROVED.equals(status);
    }

    public boolean isPending() {
        return GroupMembershipStatus.PENDING.equals(status);
    }

    public boolean isOwner() {
        return GroupRole.OWNER.equals(role);
    }

    public boolean isModeratorOrAbove() {
        return GroupRole.OWNER.equals(role) || GroupRole.MODERATOR.equals(role);
    }
}
