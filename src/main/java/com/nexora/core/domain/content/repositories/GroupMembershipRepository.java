package com.nexora.core.domain.content.repositories;

import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.enums.GroupMembershipStatus;
import com.nexora.core.domain.content.enums.GroupRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMembershipRepository {
    List<GroupMembership> findByGroupId(UUID groupId);
    List<GroupMembership> findByUserId(UUID userId);
    Optional<GroupMembership> findById(UUID id);
    Optional<GroupMembership> findByGroupIdAndUserId(UUID groupId, UUID userId);
    GroupMembership save(GroupMembership membership);
    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);
    void deleteAllByGroupId(UUID groupId);
    long countByGroupId(UUID groupId);
    long countByUserId(UUID userId);
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);
    long countByUserIdAndStatus(UUID userId, GroupMembershipStatus status);
}
