package com.nexora.core.infrastructure.persistence.content.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.nexora.core.infrastructure.persistence.common.entities.AuditableJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "posts")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostJpaEntity extends AuditableJpaEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private UserJpaEntity autor;

    @Column(name = "tipo_id")
    private UUID tipoId;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_official", nullable = false)
    @Builder.Default
    private Boolean isOfficial = false;

    @Column(name = "status")
    @Builder.Default
    private String status = "PUBLISHED";

    @Column(name = "location")
    private String location;

    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag", nullable = false)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // Wait, Comments will also be migrated
    // @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    // @Builder.Default
    // private List<CommentJpaEntity> comentarios = new ArrayList<>();
}
