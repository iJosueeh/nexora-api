package com.nexora.core.presentation.graphql.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import com.nexora.core.application.content.dto.FeedAuthorView;
import com.nexora.core.application.content.usecases.resources.queries.GetResourceCategoriesUseCase;
import com.nexora.core.application.content.usecases.resources.queries.GetResourcesUseCase;
import com.nexora.core.application.content.usecases.resources.queries.GetResourceByIdUseCase;
import com.nexora.core.application.content.usecases.resources.queries.GetMyResourcesUseCase;
import com.nexora.core.application.content.usecases.resources.queries.GenerateResourceDownloadUrlUseCase;
import com.nexora.core.application.content.usecases.resources.commands.CreateResourceCategoryUseCase;
import com.nexora.core.application.content.usecases.resources.commands.UpdateResourceCategoryUseCase;
import com.nexora.core.application.content.usecases.resources.commands.DeleteResourceCategoryUseCase;
import com.nexora.core.application.content.usecases.resources.commands.UpdateResourceUseCase;
import com.nexora.core.application.content.usecases.resources.commands.DeleteResourceUseCase;
import com.nexora.core.application.content.usecases.resources.commands.RateResourceUseCase;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.aggregates.ResourceCategory;
import com.nexora.core.domain.content.aggregates.ResourceRating;
import com.nexora.core.domain.content.ports.ResourceCategoryRepository;
import com.nexora.core.domain.content.ports.ResourceRatingRepository;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ResourceGraphQlController {

    private final GetResourceCategoriesUseCase getResourceCategoriesUseCase;
    private final GetResourcesUseCase getResourcesUseCase;
    private final GetResourceByIdUseCase getResourceByIdUseCase;
    private final GetMyResourcesUseCase getMyResourcesUseCase;
    private final GenerateResourceDownloadUrlUseCase generateResourceDownloadUrlUseCase;
    private final CreateResourceCategoryUseCase createResourceCategoryUseCase;
    private final UpdateResourceCategoryUseCase updateResourceCategoryUseCase;
    private final DeleteResourceCategoryUseCase deleteResourceCategoryUseCase;
    private final UpdateResourceUseCase updateResourceUseCase;
    private final DeleteResourceUseCase deleteResourceUseCase;
    private final RateResourceUseCase rateResourceUseCase;
    private final ProfileRepository profileRepository;
    private final ResourceCategoryRepository resourceCategoryRepository;
    private final ResourceRatingRepository resourceRatingRepository;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICIAL')")
    public List<ResourceCategoryView> resourceCategories(@Argument UUID careerId) {
        return getResourceCategoriesUseCase.execute(careerId).stream()
                .map(c -> new ResourceCategoryView(c.getId(), c.getName(), null))
                .toList();
    }

    @QueryMapping
    public List<AcademicResource> resources(
            @Argument int limit,
            @Argument int offset,
            @Argument UUID careerId,
            @Argument UUID categoryId,
            @Argument String type,
            @Argument UUID authorId,
            @Argument Double minRating) {
        return getResourcesUseCase.execute(careerId, categoryId, type, authorId, minRating, limit, offset);
    }

    @QueryMapping
    public AcademicResource resourceById(@Argument UUID id) {
        return getResourceByIdUseCase.execute(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<AcademicResource> myResources(@Argument int limit, @Argument int offset) {
        return getMyResourcesUseCase.execute(limit, offset);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public String resourceDownloadUrl(@Argument UUID resourceId) {
        return generateResourceDownloadUrlUseCase.execute(resourceId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceCategoryView createResourceCategory(@Argument String name, @Argument UUID careerId) {
        ResourceCategory category = createResourceCategoryUseCase.execute(name, careerId);
        return new ResourceCategoryView(category.getId(), category.getName(), null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceCategoryView updateResourceCategory(@Argument UUID id, @Argument String name, @Argument UUID careerId) {
        ResourceCategory category = updateResourceCategoryUseCase.execute(id, name, careerId);
        return new ResourceCategoryView(category.getId(), category.getName(), null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteResourceCategory(@Argument UUID id) {
        return deleteResourceCategoryUseCase.execute(id);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public AcademicResource updateResource(
            @Argument UUID id,
            @Argument String title,
            @Argument String description,
            @Argument UUID categoryId,
            @Argument String type) {
        return updateResourceUseCase.execute(id, title, description, categoryId, type);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deleteResource(@Argument UUID id) {
        return deleteResourceUseCase.execute(id);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public ResourceRating rateResource(@Argument UUID resourceId, @Argument int rating) {
        return rateResourceUseCase.execute(resourceId, rating);
    }

    @BatchMapping(typeName = "AcademicResource", field = "author")
    public Map<AcademicResource, FeedAuthorView> author(List<AcademicResource> resources) {
        List<UUID> authorIds = resources.stream().map(AcademicResource::getAuthorId).distinct().toList();
        Map<UUID, FeedAuthorView> authorMap = profileRepository.findByUserIdIn(authorIds).stream()
                .collect(Collectors.toMap(
                    Profile::getUserId,
                    p -> new FeedAuthorView(p.getUserId(), p.getUsername().value(),
                        p.getFullName() != null ? p.getFullName().value() : null,
                        p.getAvatarUrl())
                ));
        return resources.stream().collect(Collectors.toMap(
            resource -> resource,
            resource -> authorMap.get(resource.getAuthorId())
        ));
    }

    @BatchMapping(typeName = "AcademicResource", field = "category")
    public Map<AcademicResource, ResourceCategoryView> category(List<AcademicResource> resources) {
        List<UUID> categoryIds = resources.stream().map(AcademicResource::getCategoryId).distinct().toList();
        Map<UUID, ResourceCategory> categoryMap = resourceCategoryRepository.findAllByIds(categoryIds).stream()
                .collect(Collectors.toMap(ResourceCategory::getId, c -> c));
        return resources.stream().collect(Collectors.toMap(
            resource -> resource,
            resource -> {
                ResourceCategory cat = categoryMap.get(resource.getCategoryId());
                return cat != null ? new ResourceCategoryView(cat.getId(), cat.getName(), null) : null;
            }
        ));
    }

    @BatchMapping(typeName = "AcademicResource", field = "userRating")
    public Map<AcademicResource, Integer> userRating(List<AcademicResource> resources) {
        UUID currentUserId = getCurrentUserId();
        Map<AcademicResource, Integer> result = new HashMap<>();

        if (currentUserId == null) {
            resources.forEach(r -> result.put(r, null));
            return result;
        }

        for (AcademicResource resource : resources) {
            resourceRatingRepository.findByUserIdAndResourceId(currentUserId, resource.getId())
                    .ifPresentOrElse(
                        rating -> result.put(resource, rating.getRating()),
                        () -> result.put(resource, null)
                    );
        }

        return result;
    }

    private UUID getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof Jwt jwt) {
                    return UUID.fromString(jwt.getSubject());
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public record ResourceCategoryView(UUID id, String name, CourseView career) {}
    public record CourseView(UUID id, String name) {}
}
