package com.nexora.core.content.services;

import com.nexora.core.auth.services.AuthService;
import com.nexora.core.content.entity.Follow;
import com.nexora.core.content.repository.FollowRepository;
import com.nexora.core.profile.repository.ProfilesInterestsRepository;
import com.nexora.core.profile.repository.ProfilesRepository;
import com.nexora.core.security.service.SecurityService;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nexora.core.profile.entity.Profiles;
import com.nexora.core.graphql.dto.ProfileView;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private ProfilesRepository profilesRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private AuthService authService;
    @Mock
    private ProfilesInterestsRepository profilesInterestsRepository;

    @InjectMocks
    private SocialService socialService;

    private UUID currentUserId;
    private UUID targetUserId;
    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
        follower = new User();
        follower.setId(currentUserId);
        following = new User();
        following.setId(targetUserId);
    }

    @Test
    void toggleFollowShouldDeleteAndDecrementWhenAlreadyFollowing() {
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);
        when(followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId)).thenReturn(true);

        boolean result = socialService.toggleFollow(targetUserId);

        assertFalse(result);
        verify(followRepository).deleteByFollowerIdAndFollowingId(currentUserId, targetUserId);
        verify(profilesRepository).decrementFollowingCount(currentUserId);
        verify(profilesRepository).decrementFollowersCount(targetUserId);
        verify(entityManager).flush();
    }

    @Test
    void toggleFollowShouldSaveAndIncrementWhenNotFollowing() {
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);
        when(followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId)).thenReturn(false);
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(follower));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(following));

        boolean result = socialService.toggleFollow(targetUserId);

        assertTrue(result);
        verify(followRepository).save(any(Follow.class));
        verify(profilesRepository).incrementFollowingCount(currentUserId);
        verify(profilesRepository).incrementFollowersCount(targetUserId);
        verify(entityManager).flush();
    }

    @Test
    void toggleFollowShouldThrowExceptionWhenFollowingSelf() {
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);

        assertThrows(IllegalArgumentException.class, () -> socialService.toggleFollow(currentUserId));
        verifyNoInteractions(followRepository);
    }

    @Test
    void getFollowersShouldReturnBatchMappedProfileViews() {
        UUID userId = UUID.randomUUID();
        User followerUser = new User();
        followerUser.setId(UUID.randomUUID());
        followerUser.setEmail("follower@test.com");

        Profiles profile = new Profiles();
        profile.setUser(followerUser);
        profile.setUsername("follower_uname");
        profile.setFullName("Follower Name");
        profile.setBio("Bio test");
        profile.setFollowersCount(5);
        profile.setFollowingCount(10);

        when(followRepository.findFollowersByFollowingId(userId)).thenReturn(List.of(followerUser));
        when(profilesRepository.findByUser_IdIn(List.of(followerUser.getId()))).thenReturn(List.of(profile));
        when(profilesInterestsRepository.findAllByProfileIn(List.of(profile))).thenReturn(List.of());
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);
        when(followRepository.findFollowingIdsByFollowerIdAndFollowingIdsIn(currentUserId, List.of(followerUser.getId())))
                .thenReturn(List.of(followerUser.getId()));

        List<ProfileView> result = socialService.getFollowers(userId);

        assertEquals(1, result.size());
        ProfileView view = result.get(0);
        assertEquals(followerUser.getId(), view.id());
        assertEquals("follower@test.com", view.email());
        assertEquals("follower_uname", view.username());
        assertEquals("Follower Name", view.fullName());
        assertEquals("Bio test", view.bio());
        assertEquals(5, view.followersCount());
        assertEquals(10, view.followingCount());
        assertTrue(view.isFollowing());
    }

    @Test
    void getFollowingShouldReturnBatchMappedProfileViews() {
        UUID userId = UUID.randomUUID();
        User followingUser = new User();
        followingUser.setId(UUID.randomUUID());
        followingUser.setEmail("following@test.com");

        Profiles profile = new Profiles();
        profile.setUser(followingUser);
        profile.setUsername("following_uname");
        profile.setFullName("Following Name");
        profile.setBio("Bio test 2");
        profile.setFollowersCount(8);
        profile.setFollowingCount(12);

        when(followRepository.findFollowingByFollowerId(userId)).thenReturn(List.of(followingUser));
        when(profilesRepository.findByUser_IdIn(List.of(followingUser.getId()))).thenReturn(List.of(profile));
        when(profilesInterestsRepository.findAllByProfileIn(List.of(profile))).thenReturn(List.of());
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);
        when(followRepository.findFollowingIdsByFollowerIdAndFollowingIdsIn(currentUserId, List.of(followingUser.getId())))
                .thenReturn(List.of());

        List<ProfileView> result = socialService.getFollowing(userId);

        assertEquals(1, result.size());
        ProfileView view = result.get(0);
        assertEquals(followingUser.getId(), view.id());
        assertEquals("following@test.com", view.email());
        assertEquals("following_uname", view.username());
        assertEquals("Following Name", view.fullName());
        assertEquals("Bio test 2", view.bio());
        assertEquals(8, view.followersCount());
        assertEquals(12, view.followingCount());
        assertFalse(view.isFollowing());
    }
}
