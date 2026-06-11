package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.FollowJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FollowJpaRepository extends JpaRepository<FollowJpaEntity, UUID> {

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    @Query("SELECT f.followerId FROM FollowJpaEntity f WHERE f.followingId = :followingId")
    List<UUID> findFollowerIdsByFollowingId(UUID followingId);

    @Query("SELECT f.followingId FROM FollowJpaEntity f WHERE f.followerId = :followerId")
    List<UUID> findFollowingIdsByFollowerId(UUID followerId);

    @Query("SELECT f.followingId FROM FollowJpaEntity f WHERE f.followerId = :followerId AND f.followingId IN :followingIds")
    List<UUID> findFollowingIdsByFollowerIdAndFollowingIdsIn(UUID followerId, List<UUID> followingIds);

    long countByFollowerId(UUID followerId);

    long countByFollowingId(UUID followingId);

    @Modifying
    @Query("DELETE FROM FollowJpaEntity f WHERE f.followerId = :followerId AND f.followingId = :followingId")
    void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
}
