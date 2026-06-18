package com.nexora.core.application.content.usecases.studygroups.queries;

import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.enums.GroupMembershipStatus;
import com.nexora.core.domain.content.enums.GroupRole;
import com.nexora.core.domain.content.repositories.GroupMembershipRepository;
import com.nexora.core.domain.content.repositories.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStudyGroupsUseCase {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public List<StudyGroup> execute(int limit, int offset, String category, UUID currentUserId) {
        List<StudyGroup> groups = studyGroupRepository.findAll(limit, offset, category);

        if (currentUserId != null) {
            for (StudyGroup group : groups) {
                enrichGroupMembership(group, currentUserId);
            }
        }

        return groups;
    }

    private void enrichGroupMembership(StudyGroup group, UUID userId) {
        GroupMembership membership = groupMembershipRepository
                .findByGroupIdAndUserId(group.getId(), userId)
                .orElse(null);

        List<StudyGroup.GroupMembershipInfo> memberships = new ArrayList<>();
        if (membership != null) {
            memberships.add(new StudyGroup.GroupMembershipInfo(
                    membership.getUserId(), membership.getRole(), membership.getStatus()));
            group.setMemberIds(group.getMemberIds() != null ? group.getMemberIds() : new ArrayList<>());
            if (membership.isApproved() && !group.getMemberIds().contains(userId)) {
                group.getMemberIds().add(userId);
            }
            group.setCurrentUserIsMember(membership.isApproved());
            group.setCurrentUserRole(membership.getRole());
        } else {
            group.setCurrentUserIsMember(false);
            group.setCurrentUserRole(null);
        }
        group.setMemberships(memberships);
    }
}
