package com.nexora.core.infrastructure.persistence.notification.repositories;

import com.nexora.core.infrastructure.persistence.notification.entities.NotificationJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByUserOrderByCreatedAtDesc(UserJpaEntity user, Pageable pageable);

    long countByUserAndIsReadFalse(UserJpaEntity user);

    @Modifying
    @Query("UPDATE NotificationJpaEntity n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    void markAllAsRead(@Param("user") UserJpaEntity user);
}
