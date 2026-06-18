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
public class GetMyGroupsUseCase {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public List<StudyGroup> execute(UUID userId) {
        List<GroupMembership> memberships = groupMembershipRepository.findByUserId(userId);

        List<StudyGroup> groups = new ArrayList<>();
        for (GroupMembership membership : memberships) {
            if (!membership.isApproved()) continue;

            studyGroupRepository.findById(membership.getGroupId()).ifPresent(group -> {
                group.setMemberships(new ArrayList<>(List.of(
                        new StudyGroup.GroupMembershipInfo(
                                membership.getUserId(), membership.getRole(), membership.getStatus()))));
                groups.add(group);
            });
        }

        return groups;
    }
}
