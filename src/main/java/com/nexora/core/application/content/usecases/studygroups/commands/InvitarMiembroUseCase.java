package com.nexora.core.application.content.usecases.studygroups.commands;

import com.nexora.core.application.content.usecases.studygroups.queries.GroupInvitationView;
import com.nexora.core.domain.content.aggregates.GroupInvitation;
import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.repositories.GroupInvitationRepository;
import com.nexora.core.domain.content.repositories.GroupMembershipRepository;
import com.nexora.core.domain.content.repositories.StudyGroupRepository;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitarMiembroUseCase {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final ProfileRepository profileRepository;

    public GroupInvitationView execute(UUID groupId, String invitedUsername, UUID inviterUserId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        GroupMembership inviterMembership = groupMembershipRepository
                .findByGroupIdAndUserId(groupId, inviterUserId)
                .orElseThrow(() -> new RuntimeException("No eres miembro de este grupo"));

        if (!inviterMembership.isModeratorOrAbove()) {
            throw new RuntimeException("Solo el propietario o moderador puede invitar miembros");
        }

        Profile invitedProfile = profileRepository.findByUsername(invitedUsername)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + invitedUsername));

        UUID invitedUserId = invitedProfile.getUserId();

        if (group.hasMember(invitedUserId)) {
            throw new RuntimeException("El usuario ya es miembro de este grupo");
        }

        if (groupInvitationRepository.findByGroupIdAndInvitedUserId(groupId, invitedUserId)
                .filter(i -> i.isPending())
                .isPresent()) {
            throw new RuntimeException("Ya existe una invitación pendiente para este usuario");
        }

        GroupInvitation invitation = GroupInvitation.create(groupId, inviterUserId, invitedUserId);
        GroupInvitation saved = groupInvitationRepository.save(invitation);

        Profile inviterProfile = profileRepository.findByUserId(inviterUserId).orElse(null);

        return new GroupInvitationView(
                saved.getId(),
                saved.getGroupId(),
                group.getName(),
                group.getSlug(),
                inviterProfile != null ? inviterProfile.getUsername().value() : null,
                inviterProfile != null && inviterProfile.getFullName() != null
                        ? inviterProfile.getFullName().value() : null,
                inviterProfile != null ? inviterProfile.getAvatarUrl() : null,
                invitedProfile.getUsername().value(),
                invitedProfile.getFullName() != null ? invitedProfile.getFullName().value() : null,
                invitedProfile.getAvatarUrl(),
                saved.getStatus().name(),
                saved.getInvitedUserId()
        );
    }
}
