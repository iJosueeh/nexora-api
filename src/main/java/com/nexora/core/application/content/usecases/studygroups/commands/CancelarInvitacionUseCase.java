package com.nexora.core.application.content.usecases.studygroups.commands;

import com.nexora.core.domain.content.repositories.GroupInvitationRepository;
import com.nexora.core.domain.content.repositories.GroupMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CancelarInvitacionUseCase {

    private final GroupInvitationRepository groupInvitationRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public boolean execute(UUID invitationId, UUID currentUserId) {
        var invitation = groupInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitación no encontrada"));

        var membership = groupMembershipRepository
                .findByGroupIdAndUserId(invitation.getGroupId(), currentUserId)
                .orElseThrow(() -> new RuntimeException("No eres miembro de este grupo"));

        if (!membership.isModeratorOrAbove()) {
            throw new RuntimeException("Solo el propietario o moderador puede cancelar invitaciones");
        }

        groupInvitationRepository.deleteByGroupIdAndInvitedUserId(
                invitation.getGroupId(), invitation.getInvitedUserId());
        return true;
    }
}
