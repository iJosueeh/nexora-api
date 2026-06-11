package com.nexora.core.infrastructure.persistence.user.mappers;

import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.valueobjects.Bio;
import com.nexora.core.domain.user.valueobjects.Career;
import com.nexora.core.domain.user.valueobjects.FullName;
import com.nexora.core.domain.user.valueobjects.Username;
import com.nexora.core.infrastructure.persistence.user.entities.ProfileJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ProfileMapper {

    public Profile toDomain(ProfileJpaEntity entity) {
        if (entity == null) return null;

        Profile.ProfileBuilder<?, ?> builder = Profile.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(new Username(entity.getUsername()))
                .fullName(entity.getFullName() != null ? new FullName(entity.getFullName()) : null)
                .bio(entity.getBio() != null ? new Bio(entity.getBio()) : null)
                .avatarUrl(entity.getAvatarUrl())
                .bannerUrl(entity.getBannerUrl())
                .followersCount(entity.getFollowersCount())
                .followingCount(entity.getFollowingCount())
                .academicInterests(entity.getAcademicInterestIds() != null ? 
                    new ArrayList<>(entity.getAcademicInterestIds().stream()
                        .map(Object::toString)
                        .toList()) : new ArrayList<>());

        if (entity.getCarrera() != null) {
            builder.career(new Career(
                    entity.getCarrera().getId(),
                    entity.getCarrera().getName(),
                    entity.getCarrera().getFacultad().getId()
            ));
        }

        return builder.build();
    }

    public ProfileJpaEntity toJpa(Profile domain) {
        if (domain == null) return null;

        var builder = ProfileJpaEntity.builder()
                .userId(domain.getUserId())
                .username(domain.getUsername().value())
                .fullName(domain.getFullName() != null ? domain.getFullName().value() : null)
                .bio(domain.getBio() != null ? domain.getBio().value() : null)
                .avatarUrl(domain.getAvatarUrl())
                .bannerUrl(domain.getBannerUrl())
                .followersCount(domain.getFollowersCount())
                .followingCount(domain.getFollowingCount())
                .academicInterestIds(domain.getAcademicInterests() != null ? 
                    new ArrayList<>(domain.getAcademicInterests().stream()
                        .map(UUID::fromString)
                        .toList()) : new ArrayList<>());

        ProfileJpaEntity entity = builder.build();
        entity.setId(domain.getId());
        return entity;
    }
}
