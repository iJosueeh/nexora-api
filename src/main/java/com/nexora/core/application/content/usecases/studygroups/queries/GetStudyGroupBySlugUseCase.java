package com.nexora.core.application.content.usecases.studygroups.queries;

import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.repositories.GroupMembershipRepository;
import com.nexora.core.domain.content.repositories.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStudyGroupBySlugUseCase {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public Optional<StudyGroup> execute(String slug, UUID currentUserId) {
        Optional<StudyGroup> groupOpt = studyGroupRepository.findBySlug(slug);

        if (groupOpt.isPresent() && currentUserId != null) {
            StudyGroup group = groupOpt.get();
            enrichGroupMembership(group, currentUserId);
        }

        if (groupOpt.isPresent()) {
            StudyGroup group = groupOpt.get();
            List<GroupMembership> allMemberships = groupMembershipRepository.findByGroupId(group.getId());
            List<UUID> allMemberIds = new ArrayList<>();
            List<StudyGroup.GroupMembershipInfo> membershipInfos = new ArrayList<>();
            for (GroupMembership m : allMemberships) {
                if (m.isApproved()) {
                    allMemberIds.add(m.getUserId());
                }
                membershipInfos.add(new StudyGroup.GroupMembershipInfo(
                        m.getUserId(), m.getRole(), m.getStatus()));
            }
            group.setMemberIds(allMemberIds);
            group.setMemberships(membershipInfos);
        }

        return groupOpt;
    }

    private void enrichGroupMembership(StudyGroup group, UUID userId) {
        List<GroupMembership> allMemberships = groupMembershipRepository.findByGroupId(group.getId());
        GroupMembership currentUserMembership = null;
        for (GroupMembership m : allMemberships) {
            if (userId != null && m.getUserId().equals(userId)) {
                currentUserMembership = m;
                break;
            }
        }
        if (currentUserMembership != null) {
            group.setCurrentUserIsMember(currentUserMembership.isApproved());
            group.setCurrentUserRole(currentUserMembership.getRole());
        } else {
            group.setCurrentUserIsMember(false);
            group.setCurrentUserRole(null);
        }
    }
}
