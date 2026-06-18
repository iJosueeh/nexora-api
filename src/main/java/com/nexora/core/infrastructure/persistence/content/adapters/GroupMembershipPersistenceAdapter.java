package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.enums.GroupMembershipStatus;
import com.nexora.core.domain.content.repositories.GroupMembershipRepository;
import com.nexora.core.infrastructure.persistence.content.entities.GroupMembershipJpaEntity;
import com.nexora.core.infrastructure.persistence.content.entities.StudyGroupJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.GroupMembershipMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.GroupMembershipJpaRepository;
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
public class GroupMembershipPersistenceAdapter implements GroupMembershipRepository {

    private final GroupMembershipJpaRepository groupMembershipJpaRepository;
    private final GroupMembershipMapper groupMembershipMapper;
    private final StudyGroupJpaRepository studyGroupJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public List<GroupMembership> findByGroupId(UUID groupId) {
        return groupMembershipJpaRepository.findByGroupId(groupId).stream()
                .map(groupMembershipMapper::toDomain)
                .toList();
    }

    @Override
    public List<GroupMembership> findByUserId(UUID userId) {
        return groupMembershipJpaRepository.findByUserId(userId).stream()
                .map(groupMembershipMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<GroupMembership> findById(UUID id) {
        return groupMembershipJpaRepository.findById(id).map(groupMembershipMapper::toDomain);
    }

    @Override
    public Optional<GroupMembership> findByGroupIdAndUserId(UUID groupId, UUID userId) {
        return groupMembershipJpaRepository.findByGroupIdAndUserId(groupId, userId)
                .map(groupMembershipMapper::toDomain);
    }

    @Override
    @Transactional
    public GroupMembership save(GroupMembership membership) {
        GroupMembershipJpaEntity entity = groupMembershipMapper.toJpa(membership);

        if (membership.getGroupId() != null) {
            StudyGroupJpaEntity group = studyGroupJpaRepository.findById(membership.getGroupId()).orElse(null);
            entity.setGroup(group);
        }

        if (membership.getUserId() != null) {
            UserJpaEntity user = userJpaRepository.findById(membership.getUserId()).orElse(null);
            entity.setUser(user);
        }

        GroupMembershipJpaEntity saved = groupMembershipJpaRepository.save(entity);
        return groupMembershipMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteByGroupIdAndUserId(UUID groupId, UUID userId) {
        groupMembershipJpaRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Override
    @Transactional
    public void deleteAllByGroupId(UUID groupId) {
        groupMembershipJpaRepository.deleteAllByGroupId(groupId);
    }

    @Override
    public long countByGroupId(UUID groupId) {
        return groupMembershipJpaRepository.countByGroupId(groupId);
    }

    @Override
    public long countByUserId(UUID userId) {
        return groupMembershipJpaRepository.countByUserId(userId);
    }

    @Override
    public boolean existsByGroupIdAndUserId(UUID groupId, UUID userId) {
        return groupMembershipJpaRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public long countByUserIdAndStatus(UUID userId, GroupMembershipStatus status) {
        return groupMembershipJpaRepository.countByUserIdAndStatus(userId, status.name());
    }
}
