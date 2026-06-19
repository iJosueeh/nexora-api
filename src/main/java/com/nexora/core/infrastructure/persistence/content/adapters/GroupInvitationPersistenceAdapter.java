package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.GroupInvitation;
import com.nexora.core.domain.content.enums.GroupInvitationStatus;
import com.nexora.core.domain.content.repositories.GroupInvitationRepository;
import com.nexora.core.infrastructure.persistence.content.entities.GroupInvitationJpaEntity;
import com.nexora.core.infrastructure.persistence.content.entities.StudyGroupJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.GroupInvitationMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.GroupInvitationJpaRepository;
import com.nexora.core.infrastructure.persistence.content.repositories.StudyGroupJpaRepository;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import com.nexora.core.infrastructure.persistence.user.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GroupInvitationPersistenceAdapter implements GroupInvitationRepository {

    private final GroupInvitationJpaRepository groupInvitationJpaRepository;
    private final GroupInvitationMapper groupInvitationMapper;
    private final StudyGroupJpaRepository studyGroupJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public List<GroupInvitation> findByGroupId(UUID groupId) {
        return groupInvitationJpaRepository.findByGroupId(groupId).stream()
                .map(groupInvitationMapper::toDomain)
                .toList();
    }

    @Override
    public List<GroupInvitation> findByInvitedUserId(UUID invitedUserId) {
        return groupInvitationJpaRepository.findByInvitedUserId(invitedUserId).stream()
                .map(groupInvitationMapper::toDomain)
                .toList();
    }

    @Override
    public List<GroupInvitation> findByInvitedUserIdAndStatus(UUID invitedUserId, GroupInvitationStatus status) {
        return groupInvitationJpaRepository.findByInvitedUserIdAndStatus(invitedUserId, status.name()).stream()
                .map(groupInvitationMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<GroupInvitation> findById(UUID id) {
        return groupInvitationJpaRepository.findById(id).map(groupInvitationMapper::toDomain);
    }

    @Override
    public Optional<GroupInvitation> findByGroupIdAndInvitedUserId(UUID groupId, UUID invitedUserId) {
        return groupInvitationJpaRepository.findByGroupIdAndInvitedUserId(groupId, invitedUserId)
                .map(groupInvitationMapper::toDomain);
    }

    @Override
    @Transactional
    public GroupInvitation save(GroupInvitation invitation) {
        GroupInvitationJpaEntity entity = groupInvitationMapper.toJpa(invitation);

        if (invitation.getGroupId() != null) {
            StudyGroupJpaEntity group = studyGroupJpaRepository.findById(invitation.getGroupId()).orElse(null);
            entity.setGroup(group);
        }

        if (invitation.getInviterId() != null) {
            UserJpaEntity inviter = userJpaRepository.findById(invitation.getInviterId()).orElse(null);
            entity.setInviter(inviter);
        }

        if (invitation.getInvitedUserId() != null) {
            UserJpaEntity invitedUser = userJpaRepository.findById(invitation.getInvitedUserId()).orElse(null);
            entity.setInvitedUser(invitedUser);
        }

        GroupInvitationJpaEntity saved = groupInvitationJpaRepository.save(entity);
        return groupInvitationMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteByGroupIdAndInvitedUserId(UUID groupId, UUID invitedUserId) {
        groupInvitationJpaRepository.deleteByGroupIdAndInvitedUserId(groupId, invitedUserId);
    }

    @Override
    public boolean existsByGroupIdAndInvitedUserId(UUID groupId, UUID invitedUserId) {
        return groupInvitationJpaRepository.existsByGroupIdAndInvitedUserId(groupId, invitedUserId);
    }
}
