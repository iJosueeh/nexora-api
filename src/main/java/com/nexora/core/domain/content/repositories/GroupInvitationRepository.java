package com.nexora.core.domain.content.repositories;

import com.nexora.core.domain.content.aggregates.GroupInvitation;
import com.nexora.core.domain.content.enums.GroupInvitationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupInvitationRepository {
    List<GroupInvitation> findByGroupId(UUID groupId);
    List<GroupInvitation> findByInvitedUserId(UUID invitedUserId);
    List<GroupInvitation> findByInvitedUserIdAndStatus(UUID invitedUserId, GroupInvitationStatus status);
    Optional<GroupInvitation> findById(UUID id);
    Optional<GroupInvitation> findByGroupIdAndInvitedUserId(UUID groupId, UUID invitedUserId);
    GroupInvitation save(GroupInvitation invitation);
    void deleteByGroupIdAndInvitedUserId(UUID groupId, UUID invitedUserId);
    boolean existsByGroupIdAndInvitedUserId(UUID groupId, UUID invitedUserId);
}
