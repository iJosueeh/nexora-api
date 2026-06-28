package com.nexora.core.application.management.usecases.commands;

import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.content.repositories.PostRepository;
import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeletePostUseCase {

    private final PostRepository postRepository;
    private final SecurityService securityService;

    public boolean execute(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        UUID currentUserId = securityService.getCurrentUserId();

        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !post.getAutor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("No eres el autor de esta publicación");
        }

        postRepository.deleteById(post.getId());
        return true;
    }
}
