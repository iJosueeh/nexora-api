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
        List<GroupMembership> allMemberships = groupMembershipRepository.findByGroupId(group.getId());
        List<UUID> allMemberIds = new ArrayList<>();
        List<StudyGroup.GroupMembershipInfo> membershipInfos = new ArrayList<>();
        GroupMembership currentUserMembership = null;
        for (GroupMembership m : allMemberships) {
            if (m.isApproved()) {
                allMemberIds.add(m.getUserId());
            }
            membershipInfos.add(new StudyGroup.GroupMembershipInfo(
                    m.getUserId(), m.getRole(), m.getStatus()));
            if (userId != null && m.getUserId().equals(userId)) {
                currentUserMembership = m;
            }
        }
        group.setMemberIds(allMemberIds);

        if (currentUserMembership != null) {
            group.setCurrentUserIsMember(currentUserMembership.isApproved());
            group.setCurrentUserRole(currentUserMembership.getRole());
        } else {
            group.setCurrentUserIsMember(false);
            group.setCurrentUserRole(null);
        }
        group.setMemberships(membershipInfos);
    }
}
