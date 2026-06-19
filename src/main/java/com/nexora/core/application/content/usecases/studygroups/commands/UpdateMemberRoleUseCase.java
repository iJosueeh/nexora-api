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
public class UpdateMemberRoleUseCase {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public GroupMembership execute(UUID groupId, UUID targetUserId, GroupRole newRole, UUID requesterUserId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        GroupMembership requesterMembership = groupMembershipRepository
                .findByGroupIdAndUserId(groupId, requesterUserId)
                .orElseThrow(() -> new RuntimeException("No eres miembro de este grupo"));

        if (!requesterMembership.isModeratorOrAbove()) {
            throw new RuntimeException("Solo el propietario o moderador puede cambiar roles");
        }

        GroupMembership targetMembership = groupMembershipRepository
                .findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("El usuario no es miembro de este grupo"));

        if (targetMembership.getRole() == GroupRole.OWNER) {
            throw new RuntimeException("No se puede cambiar el rol del propietario");
        }

        if (newRole == GroupRole.OWNER) {
            throw new RuntimeException("Usa la función de transferir propiedad para cambiar el propietario");
        }

        if (requesterMembership.getRole() == GroupRole.MODERATOR && newRole == GroupRole.MODERATOR) {
            throw new RuntimeException("Un moderador no puede promover a otro moderador");
        }

        targetMembership.setRole(newRole);
        return groupMembershipRepository.save(targetMembership);
    }
}
