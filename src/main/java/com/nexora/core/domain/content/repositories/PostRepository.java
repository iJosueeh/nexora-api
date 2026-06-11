package com.nexora.core.domain.content.repositories;

import com.nexora.core.domain.content.aggregates.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {
    List<Post> findAllByOrderByCreatedAtDesc();
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Optional<Post> findById(UUID id);
    Post save(Post post);
    void deleteById(UUID id);
    long count();
}
