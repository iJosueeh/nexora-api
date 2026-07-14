package com.nexora.core.application.auth.usecases.queries;

import com.nexora.core.application.auth.dto.AuthResponse;
import com.nexora.core.domain.content.repositories.FollowRepository;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GetPublicProfileUseCase {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final FollowRepository followRepository;

    public AuthResponse execute(String username, String viewerEmail) {
        Profile profile = profileRepository.searchByUsername(username, 1).stream()
                .filter(p -> p.getUsername().value().equalsIgnoreCase(username))
                .findFirst()
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

    private boolean isProfileComplete(Profile profile) {
        if (profile == null) return false;
        return profile.getUsername() != null
                && profile.getUsername().value() != null && !profile.getUsername().value().isBlank()
                && profile.getFullName() != null
                && profile.getCareer() != null
                && profile.getAcademicInterests() != null && !profile.getAcademicInterests().isEmpty();
    }
}
