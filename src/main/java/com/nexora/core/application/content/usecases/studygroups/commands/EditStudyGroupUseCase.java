package com.nexora.core.application.content.usecases.studygroups.commands;

import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.repositories.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EditStudyGroupUseCase {

    private final StudyGroupRepository studyGroupRepository;

    public StudyGroup execute(UUID groupId, UUID userId, String name, String description,
                               String category, Boolean isPrivate, Integer maxMembers) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        if (!group.isOwner(userId)) {
            throw new RuntimeException("Solo el propietario puede editar el grupo");
        }

        if (name != null && !name.isBlank()) {
            if (name.length() < 3 || name.length() > 100) {
                throw new IllegalArgumentException("El nombre debe tener entre 3 y 100 caracteres");
            }
            group.setName(name.trim());
        }
        if (description != null) {
            group.setDescription(description);
        }
        if (category != null) {
            group.setCategory(category);
        }
        if (isPrivate != null) {
            group.setPrivate(isPrivate);
        }
        if (maxMembers != null && maxMembers > 0) {
            group.setMaxMembers(maxMembers);
        }

        StudyGroup saved = studyGroupRepository.save(group);
        saved.setCurrentUserIsMember(true);
        saved.setCurrentUserRole(com.nexora.core.domain.content.enums.GroupRole.OWNER);
        return saved;
    }
}
