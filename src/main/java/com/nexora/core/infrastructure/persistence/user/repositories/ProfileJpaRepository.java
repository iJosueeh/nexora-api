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

    @Query("SELECT p FROM ProfileJpaEntity p WHERE LOWER(p.username) = LOWER(:username)")
    Optional<ProfileJpaEntity> findByUsernameIgnoreCase(@Param("username") String username);

    @Query("SELECT p FROM ProfileJpaEntity p WHERE LOWER(p.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<ProfileJpaEntity> searchByUsername(@Param("query") String query, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT p FROM ProfileJpaEntity p WHERE p.userId NOT IN :excludeIds ORDER BY p.followersCount DESC")
    List<ProfileJpaEntity> findDiscoverableByUserIdNotIn(@Param("excludeIds") List<UUID> excludeIds, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT c.name, COUNT(p) FROM ProfileJpaEntity p JOIN p.carrera c GROUP BY c.name ORDER BY COUNT(p) DESC")
    List<Object[]> countProfilesByCareer();

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
