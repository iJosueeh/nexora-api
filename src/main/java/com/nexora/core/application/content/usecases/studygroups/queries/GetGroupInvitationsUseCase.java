package com.nexora.core.application.content.usecases.studygroups.queries;

import com.nexora.core.domain.content.aggregates.GroupInvitation;
import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.repositories.GroupInvitationRepository;
import com.nexora.core.domain.content.repositories.GroupMembershipRepository;
import com.nexora.core.domain.content.repositories.StudyGroupRepository;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetGroupInvitationsUseCase {

    private final GroupInvitationRepository groupInvitationRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final ProfileRepository profileRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public List<GroupInvitationView> execute(UUID groupId, UUID currentUserId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        var membership = groupMembershipRepository.findByGroupIdAndUserId(groupId, currentUserId)
                .orElseThrow(() -> new RuntimeException("No eres miembro de este grupo"));

        if (!membership.isModeratorOrAbove()) {
            throw new RuntimeException("Solo el propietario o moderador puede ver las invitaciones");
        }

        return groupInvitationRepository.findByGroupId(groupId).stream()
                .limit(20)
                .map(this::toView)
                .toList();
    }

    private GroupInvitationView toView(GroupInvitation invitation) {
        StudyGroup group = studyGroupRepository.findById(invitation.getGroupId()).orElse(null);
        Profile inviterProfile = profileRepository.findByUserId(invitation.getInviterId()).orElse(null);
        Profile invitedProfile = profileRepository.findByUserId(invitation.getInvitedUserId()).orElse(null);

        return new GroupInvitationView(
                invitation.getId(),
                invitation.getGroupId(),
                group != null ? group.getName() : "",
                group != null ? group.getSlug() : "",
                inviterProfile != null ? inviterProfile.getUsername().value() : null,
                inviterProfile != null && inviterProfile.getFullName() != null
                        ? inviterProfile.getFullName().value() : null,
                inviterProfile != null ? inviterProfile.getAvatarUrl() : null,
                invitedProfile != null ? invitedProfile.getUsername().value() : null,
                invitedProfile != null && invitedProfile.getFullName() != null
                        ? invitedProfile.getFullName().value() : null,
                invitedProfile != null ? invitedProfile.getAvatarUrl() : null,
                invitation.getStatus().name(),
                invitation.getInvitedUserId()
        );
    }
}
