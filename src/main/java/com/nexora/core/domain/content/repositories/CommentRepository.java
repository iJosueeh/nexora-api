package com.nexora.core.domain.content.repositories;

import com.nexora.core.domain.content.aggregates.Comment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository {
    Optional<Comment> findById(UUID id);
    List<Comment> findByPostIdOrderByCreatedAtAsc(UUID postId);
    Comment save(Comment comment);
    void delete(Comment comment);
    void deleteById(UUID id);
    List<Comment> findAllByParentIdOrderByCreatedAtAsc(UUID parentId);
}
