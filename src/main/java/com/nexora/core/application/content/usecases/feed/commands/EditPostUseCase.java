package com.nexora.core.application.content.usecases.feed.commands;

import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.content.repositories.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EditPostUseCase {

    private final PostRepository postRepository;

    public Post execute(String email, UUID postId, String titulo, String contenido, List<String> tags, String location, String imageUrl) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Authenticated user email is required");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getAutor().getEmail().value().equalsIgnoreCase(email.trim())) {
            throw new IllegalStateException("Only the author can edit this publication");
        }

        String content = contenido == null ? "" : contenido.trim();
        if (content.isBlank()) {
            throw new IllegalArgumentException("Publication content is required");
        }

        post.update(titulo, content, resolveTags(tags), location, imageUrl);
        return postRepository.save(post);
    }

    private List<String> resolveTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String raw : tags) {
            if (raw == null) continue;
            String tag = raw.replaceFirst("^#", "").trim().toLowerCase(Locale.ROOT);
            if (!tag.isEmpty()) {
                normalized.add(tag);
            }
            if (normalized.size() >= 8) break;
        }

        return new ArrayList<>(normalized);
    }
}
