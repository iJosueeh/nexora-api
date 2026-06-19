package com.nexora.core.domain.content.aggregates;

import com.nexora.core.domain.content.enums.GroupInvitationStatus;
import com.nexora.core.domain.shared.model.DomainModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
public class GroupInvitation extends DomainModel {
    private UUID groupId;
    private UUID inviterId;
    private UUID invitedUserId;
    private GroupInvitationStatus status;

    public static GroupInvitation create(UUID groupId, UUID inviterId, UUID invitedUserId) {
        GroupInvitation invitation = new GroupInvitation();
        invitation.setGroupId(groupId);
        invitation.setInviterId(inviterId);
        invitation.setInvitedUserId(invitedUserId);
        invitation.setStatus(GroupInvitationStatus.PENDING);
        return invitation;
    }

    public boolean isPending() {
        return GroupInvitationStatus.PENDING.equals(status);
    }

    public boolean isAccepted() {
        return GroupInvitationStatus.ACCEPTED.equals(status);
    }

    public boolean isRejected() {
        return GroupInvitationStatus.REJECTED.equals(status);
    }

    public void accept() {
        this.status = GroupInvitationStatus.ACCEPTED;
    }

    public void reject() {
        this.status = GroupInvitationStatus.REJECTED;
    }
}
