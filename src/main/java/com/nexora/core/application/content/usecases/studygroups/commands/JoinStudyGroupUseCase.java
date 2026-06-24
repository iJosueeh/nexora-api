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
public class JoinStudyGroupUseCase {

    private static final int MAX_GROUPS_PER_STUDENT = 10;

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public GroupMembership execute(UUID groupId, UUID userId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        if (group.hasMember(userId)) {
            throw new RuntimeException("Ya eres miembro de este grupo");
        }

        if (group.isGroupFull()) {
            throw new RuntimeException("El grupo está lleno");
        }

        long userGroupCount = groupMembershipRepository.countByUserId(userId);
        if (userGroupCount >= MAX_GROUPS_PER_STUDENT) {
            throw new RuntimeException("Has alcanzado el límite de " + MAX_GROUPS_PER_STUDENT + " grupos");
        }

        GroupMembershipStatus status = group.isPrivate()
                ? GroupMembershipStatus.PENDING
                : GroupMembershipStatus.APPROVED;

        GroupMembership membership = GroupMembership.create(
                groupId, userId, GroupRole.MEMBER, status);

        if (status == GroupMembershipStatus.APPROVED) {
            group.addMember(userId);
            studyGroupRepository.save(group);
        }

        return groupMembershipRepository.save(membership);
    }
}
