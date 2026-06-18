package com.nexora.core.application.content.usecases.studygroups.commands;

import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.enums.GroupMembershipStatus;
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
public class ApproveMembershipUseCase {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public GroupMembership execute(UUID groupId, UUID membershipId, UUID approverUserId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        GroupMembership approverMembership = groupMembershipRepository
                .findByGroupIdAndUserId(groupId, approverUserId)
                .orElseThrow(() -> new RuntimeException("No eres miembro de este grupo"));

        if (!approverMembership.isModeratorOrAbove()) {
            throw new RuntimeException("Solo el propietario o moderador puede aprobar membresías");
        }

        GroupMembership membership = groupMembershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada"));

        if (!membership.getGroupId().equals(groupId)) {
            throw new RuntimeException("La membresía no pertenece a este grupo");
        }

        if (!membership.isPending()) {
            throw new RuntimeException("Esta membresía no está pendiente");
        }

        if (group.isGroupFull()) {
            throw new RuntimeException("El grupo está lleno");
        }

        membership.setStatus(GroupMembershipStatus.APPROVED);
        group.addMember(membership.getUserId());
        studyGroupRepository.save(group);

        return groupMembershipRepository.save(membership);
    }
}
