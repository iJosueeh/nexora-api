package com.nexora.core.application.content.usecases.studygroups.commands;

import com.nexora.core.domain.content.aggregates.GroupInvitation;
import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.enums.GroupInvitationStatus;
import com.nexora.core.domain.content.enums.GroupMembershipStatus;
import com.nexora.core.domain.content.enums.GroupRole;
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

    public GroupInvitation execute(UUID groupId, String invitedUsername, UUID inviterUserId) {
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

        if (groupInvitationRepository.existsByGroupIdAndInvitedUserId(groupId, invitedUserId)) {
            throw new RuntimeException("Ya existe una invitación para este usuario");
        }

        GroupInvitation invitation = GroupInvitation.create(groupId, inviterUserId, invitedUserId);
        return groupInvitationRepository.save(invitation);
    }
}
