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
            GroupMembership membership = groupMembershipRepository
                    .findByGroupIdAndUserId(group.getId(), currentUserId)
                    .orElse(null);

            List<StudyGroup.GroupMembershipInfo> memberships = new ArrayList<>();
            if (membership != null) {
                memberships.add(new StudyGroup.GroupMembershipInfo(
                        membership.getUserId(), membership.getRole(), membership.getStatus()));
                group.setCurrentUserIsMember(membership.isApproved());
                group.setCurrentUserRole(membership.getRole());
            } else {
                group.setCurrentUserIsMember(false);
                group.setCurrentUserRole(null);
            }
            group.setMemberships(memberships);
        }

        return groupOpt;
    }
}
