package com.nexora.core.application.content.usecases.studygroups.commands;

import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.repositories.GroupMembershipRepository;
import com.nexora.core.domain.content.repositories.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveStudyGroupUseCase {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public boolean execute(UUID groupId, UUID userId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        if (group.isOwner(userId)) {
            throw new RuntimeException("El propietario no puede abandonar el grupo. Transfiere la propiedad o elimínalo.");
        }

        if (!group.hasMember(userId)) {
            throw new RuntimeException("No eres miembro de este grupo");
        }

        groupMembershipRepository.deleteByGroupIdAndUserId(groupId, userId);
        group.removeMember(userId);
        studyGroupRepository.save(group);
        return true;
    }
}
