package com.nexora.core.application.content.services;

import com.nexora.core.application.auth.services.AuthService;
import com.nexora.core.domain.content.aggregates.Follow;
import com.nexora.core.domain.content.repositories.FollowRepository;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.presentation.graphql.dto.ProfileView;
import com.nexora.core.application.security.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final FollowRepository followRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<ProfileView> getFollowers(UUID userId) {
        List<UUID> followerIds = followRepository.findFollowerIdsByFollowingId(userId);
        return convertToProfileViews(followerIds);
    }

    @Transactional(readOnly = true)
    public List<ProfileView> getFollowing(UUID userId) {
        List<UUID> followingIds = followRepository.findFollowingIdsByFollowerId(userId);
        return convertToProfileViews(followingIds);
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
            views.add(mapToProfileViewBatch(user, profile, interests, isFollowing));
        }

        return views;
    }

    private ProfileView mapToProfileViewBatch(
            User user,
            Profile profile,
            List<String> interests,
            boolean isFollowing) {

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

    @Transactional
    public boolean toggleFollow(UUID targetUserId) {
        UUID currentUserId = securityService.getCurrentUserId();

        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("You cannot follow yourself");
        }

        boolean alreadyFollowing = followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId);

        if (alreadyFollowing) {
            followRepository.deleteByFollowerIdAndFollowingId(currentUserId, targetUserId);
            updateCounters(currentUserId, targetUserId, -1);
            return false;
        } else {
            Follow follow = Follow.create(currentUserId, targetUserId);
            followRepository.save(follow);

            updateCounters(currentUserId, targetUserId, 1);
            return true;
        }
    }

    private void updateCounters(UUID followerUserId, UUID followingUserId, int delta) {
        if (delta > 0) {
            profileRepository.incrementFollowingCount(followerUserId);
            profileRepository.incrementFollowersCount(followingUserId);
        } else {
            profileRepository.decrementFollowingCount(followerUserId);
            profileRepository.decrementFollowersCount(followingUserId);
        }
    }
}
