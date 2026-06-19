package com.nexora.core.application.content.usecases.studygroups.queries;

import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.repositories.GroupMembershipRepository;
import com.nexora.core.domain.content.repositories.StudyGroupRepository;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPendingMembershipsUseCase {

    private final StudyGroupRepository studyGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final ProfileRepository profileRepository;

    public List<PendingMemberView> execute(UUID groupId, UUID currentUserId) {
        studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        GroupMembership membership = groupMembershipRepository
                .findByGroupIdAndUserId(groupId, currentUserId)
                .orElseThrow(() -> new RuntimeException("No eres miembro de este grupo"));

        if (!membership.isModeratorOrAbove()) {
            throw new RuntimeException("Solo el propietario o moderador puede ver solicitudes pendientes");
        }

        List<GroupMembership> allMemberships = groupMembershipRepository.findByGroupId(groupId);

        List<UUID> userIds = allMemberships.stream()
                .map(GroupMembership::getUserId)
                .toList();

        var profiles = profileRepository.findByUserIdIn(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        p -> p.getUserId(),
                        p -> p
                ));

        return allMemberships.stream()
                .filter(m -> m.isPending())
                .map(m -> {
                    var profile = profiles.get(m.getUserId());
                    return new PendingMemberView(
                            m.getId(),
                            m.getUserId(),
                            profile != null && profile.getUsername() != null ? profile.getUsername().value() : null,
                            profile != null && profile.getFullName() != null ? profile.getFullName().value() : "Sin nombre",
                            profile != null ? profile.getAvatarUrl() : null,
                            m.getStatus().name(),
                            m.getRole().name()
                    );
                })
                .toList();
    }
}
