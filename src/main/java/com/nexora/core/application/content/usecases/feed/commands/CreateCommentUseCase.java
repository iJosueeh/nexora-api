package com.nexora.core.application.content.usecases.feed.commands;

import com.nexora.core.domain.content.aggregates.Comment;
import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.content.repositories.CommentRepository;
import com.nexora.core.domain.content.repositories.PostRepository;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateCommentUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public Comment execute(String email, UUID postId, UUID parentId, String contenido) {
        User user = userRepository.findByEmail(new Email(email.trim()))
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Comment comment;
        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            comment = Comment.createReply(post, user, contenido, parent);
        } else {
            comment = Comment.create(post, user, contenido);
        }

        return commentRepository.save(comment);
    }
}
