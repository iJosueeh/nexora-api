package com.nexora.core.application.auth.services;

import com.nexora.core.infrastructure.persistence.user.entities.AcademicInterestJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.CourseJpaEntity;
import com.nexora.core.infrastructure.persistence.user.repositories.AcademicInterestRepository;
import com.nexora.core.infrastructure.persistence.user.repositories.CourseRepository;
import com.nexora.core.domain.content.repositories.FollowRepository;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.domain.user.valueobjects.*;
import com.nexora.core.application.security.services.SecurityService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.core.presentation.rest.auth.dto.*;
import com.nexora.core.presentation.graphql.dto.ProfileView;
import com.nexora.core.presentation.graphql.dto.UpdateProfileInput;

import lombok.RequiredArgsConstructor;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final AcademicInterestRepository academicInterestRepository;
    private final CourseRepository courseRepository;
    private final FollowRepository followRepository;
    private final SecurityService securityService;

    public RegistrationCatalogsResponse getRegistrationCatalogs() {
        List<String> careers = courseRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CourseJpaEntity::getName)
                .toList();

        List<String> academicInterests = academicInterestRepository.findAllByOrderByNameAsc()
                .stream()
                .map(AcademicInterestJpaEntity::getName)
                .toList();

        return RegistrationCatalogsResponse.builder()
                .careers(careers)
                .academicInterests(academicInterests)
                .build();
    }

    public AuthResponse completeRegistration(String email, RegisterUpdateRequest request) {
        User user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found in local database"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found for user"));

        if (request.getUsername() != null) profile.setUsername(new Username(request.getUsername()));
        if (request.getFullName() != null) profile.setFullName(new FullName(request.getFullName()));
        if (request.getBio() != null) profile.setBio(new Bio(request.getBio()));
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getBannerUrl() != null) profile.setBannerUrl(request.getBannerUrl());

        if (request.getCareer() != null && !request.getCareer().isBlank()) {
            CourseJpaEntity career = courseRepository.findByNameIgnoreCase(request.getCareer().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Career not found: " + request.getCareer()));
            profile.setCareer(new Career(career.getId(), career.getName(), career.getFacultad().getId()));
        }

        String[] incomingInterests = resolveIncomingInterests(request);
        if (incomingInterests.length > 0) {
            profile.setAcademicInterests(Arrays.asList(incomingInterests));
        }

        profileRepository.save(profile);

        return buildSessionResponse(user);
    }

    public AuthResponse resolveSession(String email, String supabaseUserId) {
        User user = upsertUserFromSupabase(email, supabaseUserId);
        ensureProfileExists(user);
        return buildSessionResponse(user);
    }

    public AuthResponse resolvePublicProfile(String username, String viewerEmail) {
        Profile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found"));

        User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found for profile"));

        syncSocialCounters(user, profile);

        List<String> academicInterests = profile.getAcademicInterests();

        boolean isFollowing = false;
        if (viewerEmail != null) {
            User viewer = userRepository.findByEmail(new Email(viewerEmail)).orElse(null);
            if (viewer != null) {
                isFollowing = followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), user.getId());
            }
        }

        return AuthResponse.builder()
                .accessToken(null)
                .tokenType(null)
                .expiresIn(0)
                .userId(user.getId())
                .email(user.getEmail().value())
                .role(user.getRole().name())
                .username(profile.getUsername().value())
                .fullName(profile.getFullName() != null ? profile.getFullName().value() : null)
                .bio(profile.getBio() != null ? profile.getBio().value() : null)
                .career(profile.getCareer() != null ? profile.getCareer().name() : null)
                .avatarUrl(profile.getAvatarUrl())
                .bannerUrl(profile.getBannerUrl())
                .followersCount(profile.getFollowersCount())
                .followingCount(profile.getFollowingCount())
                .academicInterests(academicInterests)
                .profileComplete(isProfileComplete(profile))
                .isFollowing(isFollowing)
                .build();
    }

    public ProfileView actualizarPerfil(String email, UpdateProfileInput input) {
        User user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (input.username() != null) profile.setUsername(new Username(input.username()));
        if (input.fullName() != null) profile.setFullName(new FullName(input.fullName()));
        if (input.bio() != null) profile.setBio(new Bio(input.bio()));
        if (input.avatarUrl() != null) profile.setAvatarUrl(input.avatarUrl());
        if (input.bannerUrl() != null) profile.setBannerUrl(input.bannerUrl());

        if (input.career() != null && !input.career().isBlank()) {
            CourseJpaEntity career = courseRepository.findByNameIgnoreCase(input.career().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Career not found: " + input.career()));
            profile.setCareer(new Career(career.getId(), career.getName(), career.getFacultad().getId()));
        }

        if (input.academicInterests() != null) {
            profile.setAcademicInterests(input.academicInterests());
        }

        Profile saved = profileRepository.save(profile);
        return buildProfileView(user, saved);
    }

    private void syncSocialCounters(User user, Profile profile) {
        long actualFollowers = followRepository.countByFollowingId(user.getId());
        long actualFollowing = followRepository.countByFollowerId(user.getId());

        boolean needsUpdate = false;
        if (profile.getFollowersCount() != (int) actualFollowers) {
            profile.setFollowersCount((int) actualFollowers);
            needsUpdate = true;
        }
        if (profile.getFollowingCount() != (int) actualFollowing) {
            profile.setFollowingCount((int) actualFollowing);
            needsUpdate = true;
        }

        if (needsUpdate) {
            profileRepository.save(profile);
        }
    }

    public ProfileView buildProfileView(User user, Profile profile) {
        UUID currentUserId = null;
        try {
            currentUserId = securityService.getCurrentUserId();
        } catch (Exception ignored) {}

        syncSocialCounters(user, profile);

        boolean isFollowing = false;
        if (currentUserId != null && !currentUserId.equals(user.getId())) {
            User viewer = userRepository.findById(currentUserId).orElse(null);
            if (viewer != null) {
                isFollowing = followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), user.getId());
            }
        }

        return new ProfileView(
                user.getId(),
                user.getEmail().value(),
                profile.getUsername().value(),
                profile.getFullName() != null ? profile.getFullName().value() : null,
                profile.getBio() != null ? profile.getBio().value() : null,
                profile.getCareer() != null ? profile.getCareer().name() : null,
                profile.getAvatarUrl(),
                profile.getBannerUrl(),
                profile.getFollowersCount(),
                profile.getFollowingCount(),
                profile.getAcademicInterests(),
                isProfileComplete(profile),
                isFollowing
        );
    }

    private AuthResponse buildSessionResponse(User user) {
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        if (profile != null) {
            syncSocialCounters(user, profile);
        }
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

        Profile newProfile = Profile.create(user.getId(),
                new Username(user.getEmail().value().replaceAll("@.*", "_" + user.getId().toString().substring(0, 8))));
        newProfile.setBannerUrl("");
        profileRepository.save(newProfile);
    }

    private boolean isProfileComplete(Profile profile) {
        if (profile == null) {
            return false;
        }

        return profile.getUsername() != null
                && profile.getUsername().value() != null && !profile.getUsername().value().isBlank()
                && profile.getFullName() != null
                && profile.getCareer() != null
                && profile.getAcademicInterests() != null && !profile.getAcademicInterests().isEmpty();
    }

    private String[] resolveIncomingInterests(RegisterUpdateRequest request) {
        String[] source = request.getAcademicInterests();
        if (source == null || source.length == 0) {
            source = request.getSelectedInterests();
        }

        if (source == null || source.length == 0) {
            return new String[0];
        }

        return Arrays.stream(source)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toArray(String[]::new);
    }
}
