package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.Comment;
import com.nexora.core.infrastructure.persistence.content.entities.CommentJpaEntity;
import com.nexora.core.infrastructure.persistence.user.mappers.UserMapper;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final UserMapper userMapper;

    public Comment toDomain(CommentJpaEntity entity) {
        if (entity == null) return null;

        Comment.CommentBuilder<?, ?> builder = Comment.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getAutor() != null) {
            builder.autor(userMapper.toDomain(entity.getAutor()));
        }

        if (entity.getPost() != null) {
            builder.post(PostMapper.toDomainStatic(entity.getPost()));
        }

        if (entity.getParent() != null) {
            builder.parent(toDomain(entity.getParent()));
        }

        return builder.build();
    }

    public CommentJpaEntity toJpa(Comment domain) {
        if (domain == null) return null;

        var builder = CommentJpaEntity.builder()
                .content(domain.getContent());

        if (domain.getAutor() != null) {
            builder.autor(userMapper.toJpa(domain.getAutor()));
        }

        if (domain.getPost() != null) {
            builder.post(PostMapper.toJpaStatic(domain.getPost()));
        }

        if (domain.getParent() != null) {
            builder.parent(toJpa(domain.getParent()));
        }

        CommentJpaEntity entity = builder.build();
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
