package com.nexora.core.application.content.usecases.studygroups.commands;

import com.nexora.core.common.util.SlugUtils;
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
public class CreateStudyGroupUseCase {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public StudyGroup execute(UUID userId, String name, String description,
                               String category, Boolean isPrivate, Integer maxMembers) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del grupo es obligatorio");
        }
        if (name.length() < 3 || name.length() > 100) {
            throw new IllegalArgumentException("El nombre debe tener entre 3 y 100 caracteres");
        }

        String slug = SlugUtils.makeSlug(name);
        if (studyGroupRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }

        StudyGroup group = StudyGroup.create(
                slug, name, description, category,
                Boolean.TRUE.equals(isPrivate),
                maxMembers != null ? maxMembers : 50,
                userId);

        StudyGroup saved = studyGroupRepository.save(group);

        GroupMembership ownerMembership = GroupMembership.create(
                saved.getId(), userId, GroupRole.OWNER, GroupMembershipStatus.APPROVED);
        groupMembershipRepository.save(ownerMembership);

        return saved;
    }
}
