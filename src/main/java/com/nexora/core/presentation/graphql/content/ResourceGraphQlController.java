package com.nexora.core.presentation.graphql.content;

import java.util.List;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.nexora.core.application.content.usecases.resources.queries.GetResourceCategoriesUseCase;
import com.nexora.core.application.content.usecases.resources.commands.CreateResourceCategoryUseCase;
import com.nexora.core.application.content.usecases.resources.commands.UpdateResourceCategoryUseCase;
import com.nexora.core.application.content.usecases.resources.commands.DeleteResourceCategoryUseCase;
import com.nexora.core.domain.content.aggregates.ResourceCategory;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ResourceGraphQlController {

    private final GetResourceCategoriesUseCase getResourceCategoriesUseCase;
    private final CreateResourceCategoryUseCase createResourceCategoryUseCase;
    private final UpdateResourceCategoryUseCase updateResourceCategoryUseCase;
    private final DeleteResourceCategoryUseCase deleteResourceCategoryUseCase;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICIAL')")
    public List<ResourceCategoryView> resourceCategories(@Argument UUID careerId) {
        return getResourceCategoriesUseCase.execute(careerId).stream()
                .map(c -> new ResourceCategoryView(c.getId(), c.getName(), new CourseView(c.getCareerId(), null)))
                .toList();
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceCategoryView createResourceCategory(@Argument String name, @Argument UUID careerId) {
        ResourceCategory category = createResourceCategoryUseCase.execute(name, careerId);
        return new ResourceCategoryView(category.getId(), category.getName(), new CourseView(category.getCareerId(), null));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceCategoryView updateResourceCategory(@Argument UUID id, @Argument String name, @Argument UUID careerId) {
        ResourceCategory category = updateResourceCategoryUseCase.execute(id, name, careerId);
        return new ResourceCategoryView(category.getId(), category.getName(), new CourseView(category.getCareerId(), null));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteResourceCategory(@Argument UUID id) {
        return deleteResourceCategoryUseCase.execute(id);
    }

    public record ResourceCategoryView(UUID id, String name, CourseView career) {}
    public record CourseView(UUID id, String name) {}
}
