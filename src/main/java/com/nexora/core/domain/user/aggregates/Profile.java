package com.nexora.core.domain.user.aggregates;

import com.nexora.core.domain.shared.model.DomainModel;
import com.nexora.core.domain.user.valueobjects.Bio;
import com.nexora.core.domain.user.valueobjects.Career;
import com.nexora.core.domain.user.valueobjects.FullName;
import com.nexora.core.domain.user.valueobjects.Username;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Profile extends DomainModel {
    private UUID userId;
    private Username username;
    private FullName fullName;
    private Bio bio;
    private String avatarUrl;
    private String bannerUrl;
    private Career career;
    private List<String> academicInterests;
    private int followersCount;
    private int followingCount;

    public static Profile create(UUID userId, Username username) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setUsername(username);
        profile.setAcademicInterests(new ArrayList<>());
        profile.setFollowersCount(0);
        profile.setFollowingCount(0);
        return profile;
    }

    public void updateProfile(FullName fullName, Bio bio, String avatarUrl, String bannerUrl) {
        if (fullName != null) {
            this.fullName = fullName;
        }
        if (bio != null) {
            this.bio = bio;
        }
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
        if (bannerUrl != null) {
            this.bannerUrl = bannerUrl;
        }
    }

    public void updateCareer(Career career) {
        this.career = career;
    }

    public void updateAcademicInterests(List<String> interests) {
        this.academicInterests = interests != null ? new ArrayList<>(interests) : new ArrayList<>();
    }

    public void incrementFollowers() {
        this.followersCount++;
    }

    public void decrementFollowers() {
        if (this.followersCount > 0) {
            this.followersCount--;
        }
    }

    public void incrementFollowing() {
        this.followingCount++;
    }

    public void decrementFollowing() {
        if (this.followingCount > 0) {
            this.followingCount--;
        }
    }

    public boolean hasCareer() {
        return this.career != null;
    }

    public boolean hasAcademicInterests() {
        return this.academicInterests != null && !this.academicInterests.isEmpty();
    }
}
