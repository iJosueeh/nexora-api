package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.Follow;
import com.nexora.core.domain.content.repositories.FollowRepository;
import com.nexora.core.infrastructure.persistence.content.entities.FollowJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.FollowMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.FollowJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FollowPersistenceAdapter implements FollowRepository {

    private final FollowJpaRepository followJpaRepository;
    private final FollowMapper followMapper;

    @Override
    public boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId) {
        return followJpaRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    @Transactional
    public Follow save(Follow follow) {
        FollowJpaEntity entity = followMapper.toJpa(follow);
        FollowJpaEntity saved = followJpaRepository.save(entity);
        return followMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId) {
        followJpaRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public List<UUID> findFollowerIdsByFollowingId(UUID followingId) {
        return followJpaRepository.findFollowerIdsByFollowingId(followingId);
    }

    @Override
    public List<UUID> findFollowingIdsByFollowerId(UUID followerId) {
        return followJpaRepository.findFollowingIdsByFollowerId(followerId);
    }

    @Override
    public List<UUID> findFollowingIdsByFollowerIdAndFollowingIdsIn(UUID followerId, List<UUID> followingIds) {
        return followJpaRepository.findFollowingIdsByFollowerIdAndFollowingIdsIn(followerId, followingIds);
    }

    @Override
    public long countByFollowerId(UUID followerId) {
        return followJpaRepository.countByFollowerId(followerId);
    }

    @Override
    public long countByFollowingId(UUID followingId) {
        return followJpaRepository.countByFollowingId(followingId);
    }
}
