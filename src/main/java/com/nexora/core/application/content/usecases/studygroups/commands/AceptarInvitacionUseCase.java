package com.nexora.core.application.content.usecases.studygroups.commands;

import com.nexora.core.domain.content.aggregates.GroupInvitation;
import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.enums.GroupMembershipStatus;
import com.nexora.core.domain.content.enums.GroupRole;
import com.nexora.core.domain.content.repositories.GroupInvitationRepository;
import com.nexora.core.domain.content.repositories.GroupMembershipRepository;
import com.nexora.core.domain.content.repositories.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AceptarInvitacionUseCase {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final GroupInvitationRepository groupInvitationRepository;

    public GroupMembership execute(UUID invitationId, UUID userId) {
        GroupInvitation invitation = groupInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitación no encontrada"));

        if (!invitation.getInvitedUserId().equals(userId)) {
            throw new RuntimeException("Esta invitación no es para ti");
        }

        if (!invitation.isPending()) {
            throw new RuntimeException("Esta invitación ya no está pendiente");
        }

        StudyGroup group = studyGroupRepository.findById(invitation.getGroupId())
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        if (group.isGroupFull()) {
            throw new RuntimeException("El grupo está lleno");
        }

        invitation.accept();
        groupInvitationRepository.save(invitation);

        GroupMembership membership = GroupMembership.create(
                invitation.getGroupId(),
                userId,
                GroupRole.MEMBER,
                GroupMembershipStatus.APPROVED);

        group.addMember(userId);
        studyGroupRepository.save(group);

        return groupMembershipRepository.save(membership);
    }
}
