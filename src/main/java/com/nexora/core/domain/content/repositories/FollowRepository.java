package com.nexora.core.domain.content.repositories;

import com.nexora.core.domain.content.aggregates.Follow;

import java.util.List;
import java.util.UUID;

public interface FollowRepository {
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
    Follow save(Follow follow);
    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
    List<UUID> findFollowerIdsByFollowingId(UUID followingId);
    List<UUID> findFollowingIdsByFollowerId(UUID followerId);
    List<UUID> findFollowingIdsByFollowerIdAndFollowingIdsIn(UUID followerId, List<UUID> followingIds);
    long countByFollowerId(UUID followerId);
    long countByFollowingId(UUID followingId);
}
