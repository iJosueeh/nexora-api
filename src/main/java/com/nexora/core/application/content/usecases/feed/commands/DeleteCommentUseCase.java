package com.nexora.core.application.content.usecases.feed.commands;

import com.nexora.core.domain.content.aggregates.Comment;
import com.nexora.core.domain.content.repositories.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteCommentUseCase {

    private final CommentRepository commentRepository;

    public boolean execute(String email, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getAutor().getEmail().value().equalsIgnoreCase(email.trim())) {
            throw new IllegalStateException("Only the author can delete this comment");
        }

        commentRepository.delete(comment);
        return true;
    }
}
