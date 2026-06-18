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
public class EditCommentUseCase {

    private final CommentRepository commentRepository;

    public Comment execute(String email, UUID commentId, String contenido) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getAutor().getEmail().value().equalsIgnoreCase(email.trim())) {
            throw new IllegalStateException("Only the author can edit this comment");
        }

        comment.updateContent(contenido);
        return commentRepository.save(comment);
    }
}
