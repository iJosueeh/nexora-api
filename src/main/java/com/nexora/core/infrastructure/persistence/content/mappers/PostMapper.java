package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.infrastructure.persistence.content.entities.PostJpaEntity;
import com.nexora.core.infrastructure.persistence.user.mappers.UserMapper;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final UserMapper userMapper;

    public Post toDomain(PostJpaEntity entity) {
        if (entity == null) return null;
        
        Post.PostBuilder<?, ?> builder = Post.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .content(entity.getContent())
                .isOfficial(entity.getIsOfficial())
                .status(entity.getStatus())
                .location(entity.getLocation())
                .imageUrl(entity.getImageUrl())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getAutor() != null) {
            builder.autor(userMapper.toDomain(entity.getAutor()));
        }

        return builder.build();
    }

    public PostJpaEntity toJpa(Post domain) {
        if (domain == null) return null;

        var builder = PostJpaEntity.builder()
                .titulo(domain.getTitulo())
                .content(domain.getContent())
                .isOfficial(domain.getIsOfficial())
                .status(domain.getStatus())
                .location(domain.getLocation())
                .imageUrl(domain.getImageUrl())
                .tags(domain.getTags());

        if (domain.getAutor() != null) {
            builder.autor(userMapper.toJpa(domain.getAutor()));
        }

        PostJpaEntity entity = builder.build();
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static Post toDomainStatic(PostJpaEntity entity) {
        if (entity == null) return null;

        Post.PostBuilder<?, ?> builder = Post.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .content(entity.getContent())
                .isOfficial(entity.getIsOfficial())
                .status(entity.getStatus())
                .location(entity.getLocation())
                .imageUrl(entity.getImageUrl())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        return builder.build();
    }

    public static PostJpaEntity toJpaStatic(Post domain) {
        if (domain == null) return null;

        var builder = PostJpaEntity.builder()
                .titulo(domain.getTitulo())
                .content(domain.getContent())
                .isOfficial(domain.getIsOfficial())
                .status(domain.getStatus())
                .location(domain.getLocation())
                .imageUrl(domain.getImageUrl())
                .tags(domain.getTags());

        PostJpaEntity entity = builder.build();
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
