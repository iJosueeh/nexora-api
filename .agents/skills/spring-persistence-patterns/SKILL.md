# Spring Persistence Patterns

## Entity Base Classes

### BaseJpaEntity

```java
package com.nexora.core.infrastructure.persistence.common.entities;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}
```

### AuditableJpaEntity

```java
package com.nexora.core.infrastructure.persistence.common.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class AuditableJpaEntity extends BaseJpaEntity {

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

## Repository Patterns

### JPA Repository

```java
package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.PostJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostJpaRepository extends JpaRepository<PostJpaEntity, UUID> {

    // Simple query method
    List<PostJpaEntity> findAllByOrderByCreatedAtDesc();

    // Paginated query
    Page<PostJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Custom JPQL query
    @Query("SELECT p FROM PostJpaEntity p WHERE p.autor.id = :autorId ORDER BY p.createdAt DESC")
    List<PostJpaEntity> findByAutorId(@Param("autorId") UUID autorId);

    // Native SQL for complex queries
    @Query(value = "SELECT * FROM posts WHERE content ILIKE %:search%", nativeQuery = true)
    List<PostJpaEntity> searchByContent(@Param("search") String search);

    // Count query
    long countByAutorId(UUID autorId);

    // Exists query
    boolean existsByAutorIdAndId(UUID autorId, UUID postId);
}
```

### Custom Repository Implementation

```java
package com.nexora.core.infrastructure.persistence.content.repositories;

import com.nexora.core.infrastructure.persistence.content.entities.PostJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostCustomRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<PostJpaEntity> postRowMapper = (rs, rowNum) -> {
        PostJpaEntity post = new PostJpaEntity();
        post.setId(rs.getObject("id", UUID.class));
        post.setContent(rs.getString("content"));
        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return post;
    };

    public List<PostJpaEntity> findFeedForUser(UUID userId, int limit) {
        String sql = """
            SELECT p.* FROM posts p
            INNER JOIN seguidores s ON s.following_id = p.autor_id
            WHERE s.follower_id = :userId
            ORDER BY p.created_at DESC
            LIMIT :limit
            """;
        return jdbcTemplate.query(sql, Map.of("userId", userId, "limit", limit), postRowMapper);
    }
}
```

## Mapper Patterns

### Builder Pattern (Standard)

```java
package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.infrastructure.persistence.content.entities.PostJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public Post toDomain(PostJpaEntity entity) {
        if (entity == null) return null;

        return Post.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .content(entity.getContent())
                .isOfficial(entity.getIsOfficial())
                .status(entity.getStatus())
                .location(entity.getLocation())
                .imageUrl(entity.getImageUrl())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PostJpaEntity toJpa(Post domain) {
        if (domain == null) return null;

        PostJpaEntity entity = PostJpaEntity.builder()
                .titulo(domain.getTitulo())
                .content(domain.getContent())
                .isOfficial(domain.getIsOfficial())
                .status(domain.getStatus())
                .location(domain.getLocation())
                .imageUrl(domain.getImageUrl())
                .tags(domain.getTags())
                .build();
        // Set inherited fields via setters
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
```

### Mapper with Relations

```java
package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.infrastructure.persistence.content.entities.PostJpaEntity;
import com.nexora.core.infrastructure.persistence.user.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final UserMapper userMapper;

    public Post toDomain(PostJpaEntity entity) {
        if (entity == null) return null;

        Post.PostBuilder<?, ?> builder = Post.builder()
                .id(entity.getId())
                .titulo(entity.getTitulo())
                .content(entity.getContent())
                .isOfficial(entity.getIsOfficial())
                .status(entity.getStatus())
                .location(entity.getLocation())
                .imageUrl(entity.getImageUrl())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // Map relation using UserMapper
        if (entity.getAutor() != null) {
            builder.autor(userMapper.toDomain(entity.getAutor()));
        }

        return builder.build();
    }

    public PostJpaEntity toJpa(Post domain) {
        if (domain == null) return null;

        PostJpaEntity.PostJpaEntityBuilder<?, ?> builder = PostJpaEntity.builder()
                .titulo(domain.getTitulo())
                .content(domain.getContent())
                .isOfficial(domain.getIsOfficial())
                .status(domain.getStatus())
                .location(domain.getLocation())
                .imageUrl(domain.getImageUrl())
                .tags(domain.getTags());

        // Map relation using UserMapper
        if (domain.getAutor() != null) {
            builder.autor(userMapper.toJpa(domain.getAutor()));
        }

        PostJpaEntity entity = builder.build();
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
```

## Performance Considerations

### N+1 Problem Prevention

```java
// BAD: N+1 queries
List<PostJpaEntity> posts = postJpaRepository.findAll();
for (PostJpaEntity post : posts) {
    UserJpaEntity autor = post.getAutor(); // Triggers separate query!
}

// GOOD: Use @EntityGraph
@EntityGraph(attributePaths = {"autor", "tags"})
List<PostJpaEntity> findAllWithAutorAndTags();

// GOOD: Use JOIN FETCH in JPQL
@Query("SELECT p FROM PostJpaEntity p JOIN FETCH p.autor")
List<PostJpaEntity> findAllWithAutor();

// GOOD: Use @BatchMapping in GraphQL
@BatchMapping
List<User> users(List<Post> posts) {
    // Single query for all users
}
```

### Pagination

```java
// Repository method
Page<PostJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

// Service usage
public Page<Post> getFeed(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return postJpaRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map(postMapper::toDomain);
}
```

### Caching

```java
@Service
@CacheConfig(cacheNames = "users")
public class UserService {

    @Cacheable(key = "#id")
    public User getUser(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    @CacheEvict(key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
```

## Naming Conventions

| Component | Convention | Example |
|-----------|------------|---------|
| Table | Plural, snake_case | `posts`, `usuarios`, `research_papers` |
| Column | snake_case | `created_at`, `is_official`, `autor_id` |
| JPA Entity | Suffix `JpaEntity` | `PostJpaEntity`, `UserJpaEntity` |
| JPA Repository | Suffix `JpaRepository` | `PostJpaRepository` |
| Custom Repository | Suffix `CustomRepository` | `PostCustomRepository` |
| Mapper | Suffix `Mapper` | `PostMapper`, `UserMapper` |
| Adapter | Suffix `PersistenceAdapter` | `PostPersistenceAdapter` |

## Common Pitfalls

1. **LazyInitializationException:** Use `@Transactional(readOnly = true)` on read operations
2. **N+1 Problem:** Always check for N+1 with `spring.jpa.show-sql=true`
3. **Cascade Issues:** Use `CascadeType.ALL` carefully, prefer `CascadeType.PERSIST` and `CascadeType.MERGE`
4. **FetchType.EAGER:** Avoid on collections, use `LAZY` and fetch joins
5. **Orphan Removal:** Use `orphanRemoval = true` only when child entities should be deleted with parent
