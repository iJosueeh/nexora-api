package com.nexora.core.content.entity;

import java.util.HashSet;
import java.util.Set;
import com.nexora.core.common.entity.AuditableBaseEntity;
import com.nexora.core.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "university_events")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityEvent extends AuditableBaseEntity {

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
    private Organizer organizer;

    @Embedded
    private CommunityLinks communityLinks;

    @ManyToMany
    @JoinTable(
        name = "event_attendees",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> attendees = new HashSet<>();

    @Embeddable
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Organizer {
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
    public static class CommunityLinks {
        private String whatsapp;
        private String telegram;
        private String discord;
    }
}
