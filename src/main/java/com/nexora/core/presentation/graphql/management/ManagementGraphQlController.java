package com.nexora.core.presentation.graphql.management;

import java.util.List;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.nexora.core.application.content.dto.FeedPostView;
import com.nexora.core.application.auth.dto.ProfileView;
import com.nexora.core.application.management.dto.AdminStatsView;
import com.nexora.core.application.management.usecases.queries.GetAdminStatsUseCase;
import com.nexora.core.application.management.usecases.queries.GetAllUsersUseCase;
import com.nexora.core.application.management.usecases.commands.UpdateUserStatusUseCase;
import com.nexora.core.application.management.usecases.commands.UpdateProfileAdminUseCase;
import com.nexora.core.application.management.usecases.commands.MarkPostAsOfficialUseCase;
import com.nexora.core.application.management.usecases.commands.DeletePostUseCase;
import com.nexora.core.application.management.usecases.commands.CatalogManagementUseCase;
import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.application.content.dto.FeedAuthorView;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.infrastructure.persistence.user.entities.AcademicInterestJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.CourseJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.FacultyJpaEntity;
import com.nexora.core.presentation.graphql.dto.UpdateProfileInput;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ManagementGraphQlController {

    private final GetAdminStatsUseCase getAdminStatsUseCase;
    private final GetAllUsersUseCase getAllUsersUseCase;
    private final UpdateUserStatusUseCase updateUserStatusUseCase;
    private final UpdateProfileAdminUseCase updateProfileAdminUseCase;
    private final MarkPostAsOfficialUseCase markPostAsOfficialUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final CatalogManagementUseCase catalogManagementUseCase;
    private final ProfileRepository profileRepository;

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICIAL')")
    public AdminStatsView adminStats() {
        return getAdminStatsUseCase.execute();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProfileView> allUsers(@Argument Integer limit, @Argument Integer offset, @Argument String search) {
        int safeLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
        int safeOffset = offset == null ? 0 : Math.max(0, offset);
        String safeSearch = search == null ? "" : search.trim();
        return getAllUsersUseCase.execute(safeLimit, safeOffset, safeSearch);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProfileView updateUserStatus(@Argument UUID userId, @Argument Boolean isActive) {
        return updateUserStatusUseCase.execute(userId, isActive);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICIAL')")
    public FeedPostView markPostAsOfficial(@Argument UUID postId, @Argument Boolean isOfficial) {
        Post post = markPostAsOfficialUseCase.execute(postId, isOfficial);
        return toFeedPostView(post);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deletePost(@Argument UUID postId) {
        return deletePostUseCase.execute(postId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProfileView updateProfileAdmin(@Argument UUID userId, @Argument UpdateProfileInput input) {
        return updateProfileAdminUseCase.execute(userId, input);
    }

    // Catalog Queries
    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICIAL')")
    public List<FacultyView> faculties() {
        return catalogManagementUseCase.getAllFaculties().stream()
                .map(f -> new FacultyView(f.getId(), f.getName()))
                .toList();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICIAL')")
    public List<CourseView> courses() {
        return catalogManagementUseCase.getAllCourses().stream()
                .map(c -> new CourseView(c.getId(), c.getName(),
                        new FacultyView(c.getFacultad().getId(), c.getFacultad().getName())))
                .toList();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICIAL')")
    public List<AcademicInterestView> academicInterests() {
        return catalogManagementUseCase.getAllInterests().stream()
                .map(i -> new AcademicInterestView(i.getId(), i.getName()))
                .toList();
    }

    // Catalog Mutations
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public FacultyView createFaculty(@Argument String name) {
        FacultyJpaEntity f = catalogManagementUseCase.createFaculty(name);
        return new FacultyView(f.getId(), f.getName());
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public FacultyView updateFaculty(@Argument UUID id, @Argument String name) {
        FacultyJpaEntity f = catalogManagementUseCase.updateFaculty(id, name);
        return new FacultyView(f.getId(), f.getName());
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteFaculty(@Argument UUID id) {
        return catalogManagementUseCase.deleteFaculty(id);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CourseView createCourse(@Argument String name, @Argument UUID facultyId) {
        CourseJpaEntity c = catalogManagementUseCase.createCourse(name, facultyId);
        return new CourseView(c.getId(), c.getName(),
                new FacultyView(c.getFacultad().getId(), c.getFacultad().getName()));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CourseView updateCourse(@Argument UUID id, @Argument String name, @Argument UUID facultyId) {
        CourseJpaEntity c = catalogManagementUseCase.updateCourse(id, name, facultyId);
        return new CourseView(c.getId(), c.getName(),
                new FacultyView(c.getFacultad().getId(), c.getFacultad().getName()));
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteCourse(@Argument UUID id) {
        return catalogManagementUseCase.deleteCourse(id);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AcademicInterestView createAcademicInterest(@Argument String name) {
        AcademicInterestJpaEntity i = catalogManagementUseCase.createInterest(name);
        return new AcademicInterestView(i.getId(), i.getName());
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AcademicInterestView updateAcademicInterest(@Argument UUID id, @Argument String name) {
        AcademicInterestJpaEntity i = catalogManagementUseCase.updateInterest(id, name);
        return new AcademicInterestView(i.getId(), i.getName());
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteAcademicInterest(@Argument UUID id) {
        return catalogManagementUseCase.deleteInterest(id);
    }

    // View records
    public record FacultyView(UUID id, String name) {}
    public record CourseView(UUID id, String name, FacultyView faculty) {}
    public record AcademicInterestView(UUID id, String name) {}

    private FeedPostView toFeedPostView(Post post) {
        Profile profile = profileRepository.findByUserId(post.getAutor().getId()).orElse(null);
        FeedAuthorView autor = new FeedAuthorView(
                post.getAutor().getId(),
                profile != null ? profile.getUsername().value() : null,
                profile != null && profile.getFullName() != null ? profile.getFullName().value() : "Sin nombre",
                profile != null ? profile.getAvatarUrl() : null
        );

        return new FeedPostView(
                post.getId(),
                post.getTitulo(),
                post.getContent(),
                post.getIsOfficial(),
                post.getCreatedAt().atOffset(java.time.ZoneOffset.UTC),
                0,
                0,
                false,
                autor,
                post.getTags(),
                post.getLocation(),
                post.getImageUrl()
        );
    }
}
