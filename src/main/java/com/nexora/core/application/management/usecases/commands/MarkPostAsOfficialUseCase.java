package com.nexora.core.application.management.usecases.commands;

import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.content.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MarkPostAsOfficialUseCase {

    private final PostRepository postRepository;

    public Post execute(UUID postId, boolean isOfficial) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada"));
        post.setIsOfficial(isOfficial);
        return postRepository.save(post);
    }
}
