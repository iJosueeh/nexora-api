package com.nexora.core.infrastructure.persistence.user.adapters;

import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.infrastructure.persistence.user.entities.ProfileJpaEntity;
import com.nexora.core.infrastructure.persistence.user.mappers.ProfileMapper;
import com.nexora.core.infrastructure.persistence.user.repositories.ProfileJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProfilePersistenceAdapter implements ProfileRepository {

    private final ProfileJpaRepository profileJpaRepository;
    private final ProfileMapper profileMapper;

    @Override
    public Optional<Profile> findById(UUID id) {
        return profileJpaRepository.findById(id).map(profileMapper::toDomain);
    }

    @Override
    public Optional<Profile> findByUserId(UUID userId) {
        return profileJpaRepository.findByUserId(userId).map(profileMapper::toDomain);
    }

    @Override
    public Optional<Profile> findByUsername(String username) {
        return profileJpaRepository.findByUsernameIgnoreCase(username).map(profileMapper::toDomain);
    }

    @Override
    public List<Profile> findByUserIdIn(List<UUID> userIds) {
        return profileJpaRepository.findByUserIdIn(userIds).stream()
                .map(profileMapper::toDomain)
                .toList();
    }

    @Override
    public Profile save(Profile profile) {
        ProfileJpaEntity entity = profileMapper.toJpa(profile);
        ProfileJpaEntity saved = profileJpaRepository.save(entity);
        return profileMapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        profileJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return profileJpaRepository.existsByUserId(userId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return profileJpaRepository.existsByUsername(username);
    }

    @Override
    public void incrementFollowersCount(UUID userId) {
        profileJpaRepository.incrementFollowersCount(userId);
    }

    @Override
    public void decrementFollowersCount(UUID userId) {
        profileJpaRepository.decrementFollowersCount(userId);
    }

    @Override
    public void incrementFollowingCount(UUID userId) {
        profileJpaRepository.incrementFollowingCount(userId);
    }

    @Override
    public void decrementFollowingCount(UUID userId) {
        profileJpaRepository.decrementFollowingCount(userId);
    }
}
