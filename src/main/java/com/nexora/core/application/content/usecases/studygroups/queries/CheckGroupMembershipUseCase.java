package com.nexora.core.application.content.usecases.studygroups.queries;

import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.enums.GroupMembershipStatus;
import com.nexora.core.domain.content.enums.GroupRole;
import com.nexora.core.domain.content.repositories.GroupMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckGroupMembershipUseCase {

    private final GroupMembershipRepository groupMembershipRepository;

    public boolean isMember(UUID groupId, UUID userId) {
        return groupMembershipRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    public Optional<GroupMembership> getMembership(UUID groupId, UUID userId) {
        return groupMembershipRepository.findByGroupIdAndUserId(groupId, userId);
    }
}
