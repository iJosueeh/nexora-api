package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.GroupMembershipJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMembershipJpaRepository extends JpaRepository<GroupMembershipJpaEntity, UUID> {

    List<GroupMembershipJpaEntity> findByGroupId(UUID groupId);

    List<GroupMembershipJpaEntity> findByUserId(UUID userId);

    Optional<GroupMembershipJpaEntity> findByGroupIdAndUserId(UUID groupId, UUID userId);

    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);

    void deleteAllByGroupId(UUID groupId);

    long countByGroupId(UUID groupId);

    long countByUserId(UUID userId);

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    @Query("SELECT COUNT(m) FROM GroupMembershipJpaEntity m WHERE m.user.id = :userId AND m.status = :status")
    long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);
}
