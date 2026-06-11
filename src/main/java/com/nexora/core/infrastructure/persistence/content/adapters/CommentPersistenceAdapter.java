package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.Comment;
import com.nexora.core.domain.content.repositories.CommentRepository;
import com.nexora.core.infrastructure.persistence.content.entities.CommentJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.CommentMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.CommentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements CommentRepository {

    private final CommentJpaRepository commentJpaRepository;
    private final CommentMapper commentMapper;

    @Override
    public Optional<Comment> findById(UUID id) {
        return commentJpaRepository.findById(id).map(commentMapper::toDomain);
    }

    @Override
    public List<Comment> findByPostIdOrderByCreatedAtAsc(UUID postId) {
        return commentJpaRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(commentMapper::toDomain)
                .toList();
    }

    @Override
    public Comment save(Comment comment) {
        CommentJpaEntity entity = commentMapper.toJpa(comment);
        CommentJpaEntity saved = commentJpaRepository.save(entity);
        return commentMapper.toDomain(saved);
    }

    @Override
    public void delete(Comment comment) {
        CommentJpaEntity entity = commentMapper.toJpa(comment);
        commentJpaRepository.delete(entity);
    }

    @Override
    public void deleteById(UUID id) {
        commentJpaRepository.deleteById(id);
    }

    @Override
    public List<Comment> findAllByParentIdOrderByCreatedAtAsc(UUID parentId) {
        return commentJpaRepository.findByParentIdOrderByCreatedAtAsc(parentId).stream()
                .map(commentMapper::toDomain)
                .toList();
    }
}
