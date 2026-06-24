package com.nexora.core.application.content.usecases.studygroups.queries;

import com.nexora.core.domain.content.aggregates.GroupInvitation;
import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.enums.GroupInvitationStatus;
import com.nexora.core.domain.content.repositories.GroupInvitationRepository;
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
public class GetInvitationsReceivedUseCase {

    private final GroupInvitationRepository groupInvitationRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final ProfileRepository profileRepository;

    public List<GroupInvitationView> execute(UUID userId, String status) {
        List<GroupInvitation> invitations;

        if (status != null && !status.isBlank()) {
            GroupInvitationStatus invitationStatus = GroupInvitationStatus.valueOf(status.toUpperCase());
            invitations = groupInvitationRepository.findByInvitedUserIdAndStatus(userId, invitationStatus);
        } else {
            invitations = groupInvitationRepository.findByInvitedUserId(userId);
        }

        return invitations.stream()
                .map(this::toView)
                .toList();
    }

    private GroupInvitationView toView(GroupInvitation invitation) {
        StudyGroup group = studyGroupRepository.findById(invitation.getGroupId()).orElse(null);
        Profile inviterProfile = profileRepository.findByUserId(invitation.getInviterId()).orElse(null);

        return new GroupInvitationView(
                invitation.getId(),
                invitation.getGroupId(),
                group != null ? group.getName() : "Grupo desconocido",
                group != null ? group.getSlug() : "",
                inviterProfile != null ? inviterProfile.getUsername().value() : "Usuario",
                inviterProfile != null && inviterProfile.getFullName() != null
                        ? inviterProfile.getFullName().value() : "",
                inviterProfile != null ? inviterProfile.getAvatarUrl() : null,
                null,
                null,
                null,
                invitation.getStatus().name(),
                invitation.getInvitedUserId()
        );
    }
}
