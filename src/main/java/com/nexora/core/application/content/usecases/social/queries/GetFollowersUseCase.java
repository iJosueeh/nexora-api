package com.nexora.core.application.content.usecases.social.queries;

import com.nexora.core.application.auth.dto.ProfileView;
import com.nexora.core.domain.content.repositories.FollowRepository;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.application.security.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFollowersUseCase {

    private final FollowRepository followRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;

    public List<ProfileView> execute(UUID userId) {
        List<UUID> followerIds = followRepository.findFollowerIdsByFollowingId(userId);
        return convertToProfileViews(followerIds);
    }

    private List<ProfileView> convertToProfileViews(List<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Profile> profiles = profileRepository.findByUserIdIn(userIds);
        Map<UUID, Profile> profileMap = new HashMap<>();
        for (Profile p : profiles) {
            profileMap.put(p.getUserId(), p);
        }

        List<User> users = userRepository.findAllById(userIds);
        Map<UUID, User> userMap = new HashMap<>();
        for (User u : users) {
            userMap.put(u.getId(), u);
        }

        Set<UUID> followedUserIds = new HashSet<>();
        UUID currentUserId = null;
        try {
            currentUserId = securityService.getCurrentUserId();
        } catch (Exception ignored) {}

        if (currentUserId != null) {
            followedUserIds.addAll(followRepository.findFollowingIdsByFollowerIdAndFollowingIdsIn(currentUserId, userIds));
        }

        List<ProfileView> views = new ArrayList<>(userIds.size());
        for (UUID userId : userIds) {
            Profile profile = profileMap.get(userId);
            User user = userMap.get(userId);
            List<String> interests = profile != null ? profile.getAcademicInterests() : Collections.emptyList();
            boolean isFollowing = followedUserIds.contains(userId);
            views.add(mapToProfileView(user, profile, interests, isFollowing));
        }

        return views;
    }

    private ProfileView mapToProfileView(User user, Profile profile, List<String> interests, boolean isFollowing) {
        boolean isComplete = profile != null
                && profile.getFullName() != null
                && profile.getBio() != null
                && profile.getCareer() != null
                && profile.getAvatarUrl() != null && !profile.getAvatarUrl().isBlank()
                && interests != null && !interests.isEmpty();

        return new ProfileView(
                user.getId(),
                user.getEmail().value(),
                profile != null ? profile.getUsername().value() : null,
                profile != null && profile.getFullName() != null ? profile.getFullName().value() : null,
                profile != null && profile.getBio() != null ? profile.getBio().value() : null,
                (profile != null && profile.getCareer() != null) ? profile.getCareer().name() : null,
                profile != null ? profile.getAvatarUrl() : null,
                profile != null ? profile.getBannerUrl() : null,
                profile != null ? profile.getFollowersCount() : 0,
                profile != null ? profile.getFollowingCount() : 0,
                interests != null ? interests : Collections.emptyList(),
                isComplete,
                isFollowing
        );
    }
}
