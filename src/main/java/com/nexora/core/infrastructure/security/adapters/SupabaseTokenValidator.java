package com.nexora.core.infrastructure.security.adapters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
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
        } catch (org.springframework.security.oauth2.jwt.JwtValidationException e) {
            log.debug("Token JWT invalid: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Unexpected error validating JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public record TokenPayload(String sub, String email, String role, java.util.Map<String, Object> claims) {}
}
