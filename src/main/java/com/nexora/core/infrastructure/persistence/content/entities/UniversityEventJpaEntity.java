package com.nexora.core.infrastructure.persistence.content.entities;

import com.nexora.core.infrastructure.persistence.common.entities.AuditableJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "university_events")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityEventJpaEntity extends AuditableJpaEntity {

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date", nullable = false)
    private String date;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "image_url")
    private String image;

    @Embedded
    private OrganizerJpaEmbeddable organizer;

    @Embedded
    private CommunityLinksJpaEmbeddable communityLinks;

    @ManyToMany
    @JoinTable(
        name = "event_attendees",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<UserJpaEntity> attendees = new HashSet<>();

    @Embeddable
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizerJpaEmbeddable {
        @Column(name = "organizer_name")
        private String name;
        @Column(name = "organizer_role")
        private String role;
    }

    @Embeddable
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommunityLinksJpaEmbeddable {
        private String whatsapp;
        private String telegram;
        private String discord;
    }
}
