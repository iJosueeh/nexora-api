package com.nexora.core.application.content.usecases.studygroups.commands;

import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.enums.GroupRole;
import com.nexora.core.domain.content.repositories.GroupMembershipRepository;
import com.nexora.core.domain.content.repositories.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RemoveMemberUseCase {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public boolean execute(UUID groupId, UUID targetUserId, UUID requesterUserId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        GroupMembership requesterMembership = groupMembershipRepository
                .findByGroupIdAndUserId(groupId, requesterUserId)
                .orElseThrow(() -> new RuntimeException("No eres miembro de este grupo"));

        GroupMembership targetMembership = groupMembershipRepository
                .findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("El usuario no es miembro de este grupo"));

        if (targetMembership.getUserId().equals(requesterUserId)) {
            throw new RuntimeException("No puedes removerte a ti mismo. Usa Salir del grupo.");
        }

        if (targetMembership.getRole() == GroupRole.OWNER) {
            throw new RuntimeException("No se puede remover al propietario del grupo");
        }

        boolean isOwner = requesterMembership.getRole() == GroupRole.OWNER;
        boolean isModerator = requesterMembership.getRole() == GroupRole.MODERATOR;
        boolean targetIsModerator = targetMembership.getRole() == GroupRole.MODERATOR;

        if (!isOwner && (!isModerator || targetIsModerator)) {
            throw new RuntimeException("No tienes permisos para remover a este miembro");
        }

        groupMembershipRepository.deleteByGroupIdAndUserId(groupId, targetUserId);
        group.removeMember(targetUserId);
        studyGroupRepository.save(group);

        return true;
    }
}
