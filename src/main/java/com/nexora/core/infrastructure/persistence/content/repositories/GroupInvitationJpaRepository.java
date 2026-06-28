package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.GroupInvitationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupInvitationJpaRepository extends JpaRepository<GroupInvitationJpaEntity, UUID> {

    List<GroupInvitationJpaEntity> findByGroupId(UUID groupId);

    List<GroupInvitationJpaEntity> findByInvitedUserId(UUID invitedUserId);

    @Query("SELECT i FROM GroupInvitationJpaEntity i WHERE i.invitedUser.id = :userId AND i.status = :status")
    List<GroupInvitationJpaEntity> findByInvitedUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    Optional<GroupInvitationJpaEntity> findByGroupIdAndInvitedUserId(UUID groupId, UUID invitedUserId);

    boolean existsByGroupIdAndInvitedUserId(UUID groupId, UUID invitedUserId);

    void deleteByGroupIdAndInvitedUserId(UUID groupId, UUID invitedUserId);
}
