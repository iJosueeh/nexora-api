package com.nexora.core.application.auth.usecases.commands;

import com.nexora.core.application.auth.dto.AuthResponse;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ResolveSessionUseCase {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public AuthResponse execute(String email, String supabaseUserId) {
        User user = upsertUserFromSupabase(email, supabaseUserId);
        ensureProfileExists(user);
        return buildSessionResponse(user);
    }

    private User upsertUserFromSupabase(String email, String supabaseUserId) {
        return userRepository.findByEmail(new Email(email))
                .map(existing -> {
                    if (existing.getRole() == null) {
                        existing.setRole(UserRole.ROLE_STUDENT);
                    }
                    if (existing.getIsActive() == null) {
                        existing.setIsActive(true);
                    }
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User newUser = User.create(new Email(email), UserRole.ROLE_STUDENT,
                            supabaseUserId != null ? new SupabaseId(supabaseUserId) : null);

                    if (supabaseUserId != null && !supabaseUserId.isBlank()) {
                        try {
                            newUser.setId(UUID.fromString(supabaseUserId));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }

                    return userRepository.save(newUser);
                });
    }

    private void ensureProfileExists(User user) {
        if (profileRepository.findByUserId(user.getId()).isPresent()) {
            return;
        }

        String rawUsername = user.getEmail().value().replaceAll("@.*", "")
                .replaceAll("[^a-zA-Z0-9]", "_") + "_" + user.getId().toString().substring(0, 8);
        if (rawUsername.length() < 5) {
            rawUsername = "user_" + user.getId().toString().substring(0, 8);
        }
        if (rawUsername.length() > 30) {
            rawUsername = rawUsername.substring(0, 30);
        }
        Profile newProfile = Profile.create(user.getId(), new Username(rawUsername));
        newProfile.setBannerUrl("");
        profileRepository.save(newProfile);
    }

    private AuthResponse buildSessionResponse(User user) {
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        List<String> academicInterests = profile != null ? profile.getAcademicInterests() : List.of();

        return AuthResponse.builder()
                .accessToken(null)
                .tokenType(null)
                .expiresIn(0)
                .userId(user.getId())
                .email(user.getEmail().value())
                .role(user.getRole().name())
                .username(profile != null ? profile.getUsername().value() : null)
                .fullName(profile != null && profile.getFullName() != null ? profile.getFullName().value() : null)
                .bio(profile != null && profile.getBio() != null ? profile.getBio().value() : null)
                .career(profile != null && profile.getCareer() != null ? profile.getCareer().name() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .bannerUrl(profile != null ? profile.getBannerUrl() : null)
                .followersCount(profile != null ? profile.getFollowersCount() : 0)
                .followingCount(profile != null ? profile.getFollowingCount() : 0)
                .academicInterests(academicInterests)
                .profileComplete(isProfileComplete(profile))
                .isFollowing(false)
                .build();
    }

    private boolean isProfileComplete(Profile profile) {
        if (profile == null) return false;
        return profile.getUsername() != null
                && profile.getUsername().value() != null && !profile.getUsername().value().isBlank()
                && profile.getFullName() != null
                && profile.getCareer() != null
                && profile.getAcademicInterests() != null && !profile.getAcademicInterests().isEmpty();
    }
}
