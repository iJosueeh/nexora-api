package com.nexora.core.infrastructure.persistence.user.repositories;

import com.nexora.core.infrastructure.persistence.user.entities.ProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileJpaRepository extends JpaRepository<ProfileJpaEntity, UUID> {

    Optional<ProfileJpaEntity> findByUserId(UUID userId);

    List<ProfileJpaEntity> findByUserIdIn(List<UUID> userIds);

    Optional<ProfileJpaEntity> findByUsernameIgnoreCase(String username);

    boolean existsByUserId(UUID userId);

    boolean existsByUsername(String username);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE perfiles SET followers_count = followers_count + 1 WHERE usuario_id = :userId", nativeQuery = true)
    void incrementFollowersCount(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE perfiles SET followers_count = GREATEST(0, followers_count - 1) WHERE usuario_id = :userId", nativeQuery = true)
    void decrementFollowersCount(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE perfiles SET following_count = following_count + 1 WHERE usuario_id = :userId", nativeQuery = true)
    void incrementFollowingCount(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE perfiles SET following_count = GREATEST(0, following_count - 1) WHERE usuario_id = :userId", nativeQuery = true)
    void decrementFollowingCount(@Param("userId") UUID userId);
}
