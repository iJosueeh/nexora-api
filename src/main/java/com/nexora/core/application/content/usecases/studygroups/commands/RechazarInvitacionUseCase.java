package com.nexora.core.application.content.usecases.studygroups.commands;

import com.nexora.core.domain.content.aggregates.GroupInvitation;
import com.nexora.core.domain.content.repositories.GroupInvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RechazarInvitacionUseCase {

    private final GroupInvitationRepository groupInvitationRepository;

    public boolean execute(UUID invitationId, UUID userId) {
        GroupInvitation invitation = groupInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitación no encontrada"));

        if (!invitation.getInvitedUserId().equals(userId)) {
            throw new RuntimeException("Esta invitación no es para ti");
        }

        if (!invitation.isPending()) {
            throw new RuntimeException("Esta invitación ya no está pendiente");
        }

        invitation.reject();
        groupInvitationRepository.save(invitation);
        return true;
    }
}
