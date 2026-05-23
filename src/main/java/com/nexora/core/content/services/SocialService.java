package com.nexora.core.content.services;

import com.nexora.core.auth.services.AuthService;
import com.nexora.core.content.entity.Follow;
import com.nexora.core.content.repository.FollowRepository;
import com.nexora.core.graphql.dto.ProfileView;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.profile.entity.ProfilesInterests;
import com.nexora.core.profile.repository.ProfilesInterestsRepository;
import com.nexora.core.profile.repository.ProfilesRepository;
import com.nexora.core.security.service.SecurityService;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final FollowRepository followRepository;
    private final ProfilesRepository profilesRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final EntityManager entityManager;
    private final AuthService authService;
    private final ProfilesInterestsRepository profilesInterestsRepository;

    @Transactional(readOnly = true)
    public List<ProfileView> getFollowers(UUID userId) {
        List<User> followers = followRepository.findFollowersByFollowingId(userId);
        return convertToProfileViews(followers);
    }

    @Transactional(readOnly = true)
    public List<ProfileView> getFollowing(UUID userId) {
        List<User> following = followRepository.findFollowingByFollowerId(userId);
        return convertToProfileViews(following);
    }

    private List<ProfileView> convertToProfileViews(List<User> users) {
        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> userIds = users.stream().map(User::getId).toList();

        // 1. Fetch profiles in batch
        List<Profiles> profiles = profilesRepository.findByUser_IdIn(userIds);
        Map<UUID, Profiles> profileMap = new HashMap<>();
        for (Profiles p : profiles) {
            if (p.getUser() != null) {
                profileMap.put(p.getUser().getId(), p);
            }
        }

        // 2. Fetch academic interests in batch
        Map<UUID, List<String>> interestsMap = new HashMap<>();
        if (!profiles.isEmpty()) {
            List<ProfilesInterests> allInterests = profilesInterestsRepository.findAllByProfileIn(profiles);
            for (ProfilesInterests pi : allInterests) {
                if (pi.getProfile() != null) {
                    UUID profileId = pi.getProfile().getId();
                    interestsMap.computeIfAbsent(profileId, k -> new ArrayList<>())
                                .add(pi.getInteres().getName());
                }
            }
        }

        // 3. Determine if current user follows them in batch
        Set<UUID> followedUserIds = new HashSet<>();
        UUID currentUserId = null;
        try {
            currentUserId = securityService.getCurrentUserId();
        } catch (Exception ignored) {}

        if (currentUserId != null) {
            followedUserIds.addAll(followRepository.findFollowingIdsByFollowerIdAndFollowingIdsIn(currentUserId, userIds));
        }

        // 4. Build ProfileView list
        List<ProfileView> views = new ArrayList<>(users.size());
        for (User user : users) {
            Profiles profile = profileMap.get(user.getId());
            List<String> interests = Collections.emptyList();
            if (profile != null) {
                interests = interestsMap.getOrDefault(profile.getId(), Collections.emptyList());
            }
            boolean isFollowing = followedUserIds.contains(user.getId());
            views.add(mapToProfileViewBatch(user, profile, interests, isFollowing));
        }

        return views;
    }

    private ProfileView mapToProfileViewBatch(
            User user,
            Profiles profile,
            List<String> interests,
            boolean isFollowing) {
        
        boolean isComplete = profile != null
                && profile.getFullName() != null && !profile.getFullName().isBlank()
                && profile.getBio() != null && !profile.getBio().isBlank()
                && profile.getCarrera() != null
                && profile.getAvatarUrl() != null && !profile.getAvatarUrl().isBlank()
                && interests != null && !interests.isEmpty();

        return new ProfileView(
                user.getId(),
                user.getEmail(),
                profile != null ? profile.getUsername() : null,
                profile != null ? profile.getFullName() : null,
                profile != null ? profile.getBio() : null,
                (profile != null && profile.getCarrera() != null) ? profile.getCarrera().getName() : null,
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
            entityManager.flush();
            return false;
        } else {
            User follower = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Follower not found"));
            User following = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new RuntimeException("Target user not found"));

            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(following);
            followRepository.save(follow);

            updateCounters(currentUserId, targetUserId, 1);
            entityManager.flush();
            return true;
        }
    }

    private void updateCounters(UUID followerUserId, UUID followingUserId, int delta) {
        if (delta > 0) {
            profilesRepository.incrementFollowingCount(followerUserId);
            profilesRepository.incrementFollowersCount(followingUserId);
        } else {
            profilesRepository.decrementFollowingCount(followerUserId);
            profilesRepository.decrementFollowersCount(followingUserId);
        }
    }
}
