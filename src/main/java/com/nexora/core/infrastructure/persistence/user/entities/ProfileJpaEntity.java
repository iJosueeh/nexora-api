package com.nexora.core.infrastructure.persistence.user.entities;

import com.nexora.core.infrastructure.persistence.common.entities.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "perfiles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileJpaEntity extends BaseJpaEntity {

    @Column(name = "usuario_id", unique = true, nullable = false)
    private UUID userId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "banner_url", columnDefinition = "TEXT")
    private String bannerUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrera_id")
    private CourseJpaEntity carrera;

    @Column(name = "followers_count")
    @Builder.Default
    private int followersCount = 0;

    @Column(name = "following_count")
    @Builder.Default
    private int followingCount = 0;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "perfiles_intereses", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "interes_id")
    @Builder.Default
    private List<UUID> academicInterestIds = new ArrayList<>();
}
