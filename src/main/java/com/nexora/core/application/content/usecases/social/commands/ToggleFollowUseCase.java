package com.nexora.core.application.content.usecases.social.commands;

import com.nexora.core.domain.content.aggregates.Follow;
import com.nexora.core.domain.content.repositories.FollowRepository;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.application.security.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ToggleFollowUseCase {

    private final FollowRepository followRepository;
    private final ProfileRepository profileRepository;
    private final SecurityService securityService;

    public boolean execute(UUID targetUserId) {
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
