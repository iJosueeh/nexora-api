package com.nexora.core.domain.user.repositories;

import com.nexora.core.domain.user.aggregates.Profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository {
    Optional<Profile> findById(UUID id);
    Optional<Profile> findByUserId(UUID userId);
    Optional<Profile> findByUsername(String username);
    List<Profile> searchByUsername(String query, int limit);
    List<Profile> findByUserIdIn(List<UUID> userIds);
    Profile save(Profile profile);
    void deleteById(UUID id);
    boolean existsByUserId(UUID userId);
    boolean existsByUsername(String username);
    void incrementFollowersCount(UUID userId);
    void decrementFollowersCount(UUID userId);
    void incrementFollowingCount(UUID userId);
    void decrementFollowingCount(UUID userId);
}
