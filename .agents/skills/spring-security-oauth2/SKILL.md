# Spring Security with OAuth2 and Supabase

## Authentication Flow

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Frontend  │────▶│   Supabase  │────▶│   Spring    │────▶│   Database  │
│             │     │   Auth      │     │   Security  │     │             │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
     1. Login            2. JWT              3. Validate        4. Load User
```

## Key Components

### SecurityConfig

```java
package com.nexora.core.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    auth.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                    auth.requestMatchers("/api/auth/catalogs").permitAll();
                    auth.requestMatchers("/api/auth/public-profile/**").permitAll();
                    auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();
                    auth.requestMatchers("/graphql", "/graphiql/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### Token Validation (Supabase)

```java
package com.nexora.core.infrastructure.security.adapters;

import com.nexora.core.application.auth.ports.TokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SupabaseTokenValidator implements TokenValidator {

    private final JwtDecoder jwtDecoder;

    @Override
    public Optional<TokenPayload> validate(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            TokenPayload payload = new TokenPayload(
                    jwt.getSubject(),
                    jwt.getClaimAsString("email"),
                    jwt.getClaimAsString("role")
            );
            return Optional.of(payload);
        } catch (JwtException e) {
            return Optional.empty();
        }
    }
}

public record TokenPayload(String sub, String email, String role) {}
```

### UserDetails Adapter (Domain to Spring Security)

```java
package com.nexora.core.infrastructure.security.adapters;

import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.valueobjects.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserDetailsAdapter implements UserDetails {

    private final User user;

    public UserDetailsAdapter(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority(user.getRole().name())
        );
    }

    @Override
    public String getPassword() {
        return ""; // No local password - Supabase auth
    }

    @Override
    public String getUsername() {
        return user.getEmail().value();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return user.getIsActive(); }

    public User getUser() {
        return user;
    }
}
```

### Custom UserDetailsService

```java
package com.nexora.core.infrastructure.security.adapters;

import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuario no encontrado con email: " + email));
        return new UserDetailsAdapter(user);
    }
}
```

### Security Service (Access Current User)

```java
package com.nexora.core.infrastructure.security.adapters;

import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }
        String email = auth.getName();
        return userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    public boolean isCurrentUser(User user) {
        String currentEmail = getCurrentUserEmail();
        return currentEmail != null && currentEmail.equals(user.getEmail().value());
    }
}
```

## Role-Based Access Control

### GraphQL Controller

```java
package com.nexora.core.presentation.graphql;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class NexoraMutationController {

    @PreAuthorize("hasRole('ADMIN')")
    public Post markAsOfficial(UUID postId) {
        // Only admins can mark posts as official
    }

    @PreAuthorize("isAuthenticated()")
    public Comment addComment(UUID postId, String content) {
        // Any authenticated user can add comments
    }

    @PreAuthorize("@securityService.isCurrentUser(#post.author)")
    public Post updatePost(Post post) {
        // Only the author can update their post
    }
}
```

### REST Controller

```java
package com.nexora.core.presentation.rest.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public AuthResponse getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        // Return current user info
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public List<UserResponse> getAllUsers() {
        // Admin only
    }
}
```

## Common Pitfalls

### 1. Don't Implement UserDetails on Domain Model

```java
// BAD: Domain model implements Spring Security interface
public class User extends DomainModel implements UserDetails { ... }

// GOOD: Separate adapter for Spring Security
public class User extends DomainModel { ... }
public class UserDetailsAdapter implements UserDetails {
    private final User user;
    // ...
}
```

### 2. Don't Use JWT Claims Directly in Domain

```java
// BAD: Domain depends on JWT
public class User {
    public static User fromJwt(Jwt jwt) {
        this.email = jwt.getClaimAsString("email");
    }
}

// GOOD: Domain uses value objects
public class User {
    public static User create(Email email, UserRole role) {
        this.email = email;
        this.role = role;
    }
}
```

### 3. Separate Token Validation from User Loading

```java
// BAD: Combined validation and loading
public UserDetails loadUserByJwt(Jwt jwt) {
    // Validate token AND load user
}

// GOOD: Separate concerns
public class SupabaseTokenValidator implements TokenValidator {
    public Optional<TokenPayload> validate(String token) { ... }
}

public class CustomUserDetailsService implements UserDetailsService {
    public UserDetails loadUserByUsername(String email) { ... }
}
```

### 4. Use Transactions for User Loading

```java
// BAD: No transaction - may cause LazyInitializationException
public UserDetails loadUserByUsername(String email) {
    User user = userRepository.findByEmail(email);
    return new UserDetailsAdapter(user); // user.getRole() may fail
}

// GOOD: Transactional
@Transactional(readOnly = true)
public UserDetails loadUserByUsername(String email) {
    User user = userRepository.findByEmail(email);
    return new UserDetailsAdapter(user);
}
```

## Testing Security

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminAccess() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void shouldDenyStudentAccess() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
```
