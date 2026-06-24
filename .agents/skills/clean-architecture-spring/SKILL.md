# Clean Architecture in Spring Boot

## Core Principles

- Domain layer has ZERO dependencies on infrastructure
- Use cases (application layer) depend only on domain ports
- Infrastructure implements domain ports
- Dependency rule: outer layers depend on inner layers, never reverse

## Directory Structure

```
src/main/java/com/nexora/core/
├── domain/                          # Domain layer (innermost)
│   ├── shared/model/DomainModel.java
│   ├── {feature}/aggregates/
│   ├── {feature}/valueobjects/
│   ├── {feature}/repositories/      # Port interfaces
│   └── {feature}/events/
├── application/                     # Application layer
│   ├── {feature}/usecases/
│   ├── {feature}/ports/             # Secondary ports
│   └── {feature}/dto/
├── infrastructure/                  # Infrastructure layer
│   ├── persistence/{feature}/entities/
│   ├── persistence/{feature}/repositories/
│   ├── persistence/{feature}/adapters/
│   ├── persistence/{feature}/mappers/
│   └── security/
└── presentation/                    # Presentation layer (outermost)
    ├── rest/
    └── graphql/
```

## Domain Layer Patterns

### Aggregate Root

```java
package com.nexora.core.domain.user.aggregates;

import com.nexora.core.domain.shared.model.DomainModel;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends DomainModel {
    private Email email;
    private UserRole role;
    private Boolean isActive;
    private SupabaseId supabaseId;

    public static User create(Email email, UserRole role, SupabaseId supabaseId) {
        validateEmail(email);
        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        user.setSupabaseId(supabaseId);
        user.setIsActive(true);
        return user;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void changeRole(UserRole newRole) {
        this.role = newRole;
    }

    private static void validateEmail(Email email) {
        if (email == null || email.value() == null || email.value().isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
    }
}
```

### Value Objects

```java
package com.nexora.core.domain.user.valueobjects;

import java.util.Objects;

public record Email(String value) {
    private static final String UTP_DOMAIN = "@utp.edu.pe";

    public Email {
        Objects.requireNonNull(value, "Email cannot be null");
        if (!value.contains(UTP_DOMAIN)) {
            throw new IllegalArgumentException("Email must be from UTP domain");
        }
    }

    public String username() {
        return value.split("@")[0];
    }
}
```

### Repository Ports

```java
package com.nexora.core.domain.user.repositories;

import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.valueobjects.Email;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(Email email);
    User save(User user);
    void deleteById(UUID id);
    boolean existsByEmail(Email email);
}
```

### Domain Events

```java
package com.nexora.core.domain.user.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRegistered(
    UUID userId,
    String email,
    LocalDateTime occurredAt
) {
    public static UserRegistered of(UUID userId, String email) {
        return new UserRegistered(userId, email, LocalDateTime.now());
    }
}
```

## Infrastructure Layer Patterns

### JPA Entity

```java
package com.nexora.core.infrastructure.persistence.user.entities;

import com.nexora.core.infrastructure.persistence.common.entities.AuditableJpaEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity extends AuditableJpaEntity {
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private String role;

    @Column(name = "supabase_id")
    private String supabaseId;
}
```

### Persistence Adapter

```java
package com.nexora.core.infrastructure.persistence.user.adapters;

import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.Email;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import com.nexora.core.infrastructure.persistence.user.mappers.UserMapper;
import com.nexora.core.infrastructure.persistence.user.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return userJpaRepository.findByEmail(email.value()).map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = userMapper.toJpa(user);
        UserJpaEntity saved = userJpaRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.value());
    }
}
```

### Mapper

```java
package com.nexora.core.infrastructure.persistence.user.mappers;

import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.valueobjects.Email;
import com.nexora.core.domain.user.valueobjects.UserRole;
import com.nexora.core.domain.user.valueobjects.SupabaseId;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;

        return User.builder()
                .id(entity.getId())
                .email(new Email(entity.getEmail()))
                .role(UserRole.valueOf(entity.getRole()))
                .isActive(entity.getIsActive())
                .supabaseId(entity.getSupabaseId() != null ? new SupabaseId(entity.getSupabaseId()) : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserJpaEntity toJpa(User domain) {
        if (domain == null) return null;

        UserJpaEntity entity = UserJpaEntity.builder()
                .email(domain.getEmail().value())
                .role(domain.getRole().name())
                .isActive(domain.getIsActive())
                .supabaseId(domain.getSupabaseId() != null ? domain.getSupabaseId().value() : null)
                .build();
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
```

## Anti-Patterns to Avoid

- Domain model with @Entity annotation
- Repository returning JPA entities
- Business logic in controllers or mappers
- Circular dependencies between layers
- Domain model implementing Spring interfaces (UserDetails, Serializable, etc.)
- Infrastructure annotations in domain layer

## Naming Conventions

| Layer | Convention | Example |
|-------|------------|---------|
| Domain Aggregate | Singular, PascalCase | `User`, `Post`, `ResearchPaper` |
| Value Object | Singular, PascalCase | `Email`, `Username`, `Slug` |
| Repository Port | Plural, PascalCase | `UserRepository`, `PostRepository` |
| JPA Entity | Suffix `JpaEntity` | `UserJpaEntity`, `PostJpaEntity` |
| JPA Repository | Suffix `JpaRepository` | `UserJpaRepository` |
| Adapter | Suffix `PersistenceAdapter` | `UserPersistenceAdapter` |
| Mapper | Suffix `Mapper` | `UserMapper`, `PostMapper` |
| Use Case | Verb + Noun | `CreateUser`, `GetPostFeed` |
| DTO | Suffix `Request`/`Response` | `CreateUserRequest` |
