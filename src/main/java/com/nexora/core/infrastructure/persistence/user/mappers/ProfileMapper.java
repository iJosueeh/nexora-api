package com.nexora.core.infrastructure.persistence.user.mappers;

import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.valueobjects.Bio;
import com.nexora.core.domain.user.valueobjects.Career;
import com.nexora.core.domain.user.valueobjects.FullName;
import com.nexora.core.domain.user.valueobjects.Username;
import com.nexora.core.infrastructure.persistence.user.entities.AcademicInterestJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.ProfileJpaEntity;
import com.nexora.core.infrastructure.persistence.user.repositories.AcademicInterestRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ProfileMapper {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{5,30}$");

    private final AcademicInterestRepository academicInterestRepository;

    public ProfileMapper(AcademicInterestRepository academicInterestRepository) {
        this.academicInterestRepository = academicInterestRepository;
    }

    public Profile toDomain(ProfileJpaEntity entity) {
        if (entity == null) return null;

        Profile.ProfileBuilder<?, ?> builder = Profile.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(safeUsername(entity.getUsername()))
                .fullName(entity.getFullName() != null ? new FullName(entity.getFullName()) : null)
                .bio(entity.getBio() != null ? new Bio(entity.getBio()) : null)
                .avatarUrl(entity.getAvatarUrl())
                .bannerUrl(entity.getBannerUrl())
                .followersCount(entity.getFollowersCount())
                .followingCount(entity.getFollowingCount())
                .academicInterests(resolveInterestNames(entity.getAcademicInterestIds()));

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

    private List<String> resolveInterestNames(List<UUID> interestIds) {
        if (interestIds == null || interestIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<AcademicInterestJpaEntity> interests = academicInterestRepository.findAllById(interestIds);
        Map<UUID, String> nameMap = interests.stream()
                .collect(Collectors.toMap(AcademicInterestJpaEntity::getId, AcademicInterestJpaEntity::getName));
        return interestIds.stream()
                .map(id -> nameMap.getOrDefault(id, id.toString()))
                .collect(Collectors.toList());
    }

    private static Username safeUsername(String raw) {
        if (raw != null && USERNAME_PATTERN.matcher(raw).matches()) {
            return new Username(raw);
        }
        String sanitized = raw != null
                ? raw.replaceAll("[^a-zA-Z0-9_]", "_")
                : "user";
        if (sanitized.length() < 5) {
            sanitized = sanitized + "_usr";
        }
        if (sanitized.length() > 30) {
            sanitized = sanitized.substring(0, 30);
        }
        if (!USERNAME_PATTERN.matcher(sanitized).matches()) {
            sanitized = "user_" + UUID.randomUUID().toString().substring(0, 8);
        }
        return new Username(sanitized);
    }
}
