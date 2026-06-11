package com.nexora.core.infrastructure.security.adapters;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SupabaseTokenValidator {

    private final JwtDecoder jwtDecoder;

    public Optional<TokenPayload> validate(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            TokenPayload payload = new TokenPayload(
                    jwt.getSubject(),
                    jwt.getClaimAsString("email"),
                    jwt.getClaimAsString("role"),
                    jwt.getClaims()
            );
            return Optional.of(payload);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public record TokenPayload(String sub, String email, String role, java.util.Map<String, Object> claims) {}
}
