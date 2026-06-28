package com.nexora.core.domain.content.aggregates;

import com.nexora.core.domain.shared.model.DomainModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Follow extends DomainModel {
    private UUID followerId;
    private UUID followingId;

    public static Follow create(UUID followerId, UUID followingId) {
        if (followerId == null) {
            throw new IllegalArgumentException("Follower ID cannot be null");
        }
        if (followingId == null) {
            throw new IllegalArgumentException("Following ID cannot be null");
        }
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        return follow;
    }
}
