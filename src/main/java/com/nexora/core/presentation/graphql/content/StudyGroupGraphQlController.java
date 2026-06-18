package com.nexora.core.presentation.graphql.content;

import java.util.List;
import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.nexora.core.application.content.usecases.studygroups.commands.ApproveMembershipUseCase;
import com.nexora.core.application.content.usecases.studygroups.commands.CreateStudyGroupUseCase;
import com.nexora.core.application.content.usecases.studygroups.commands.DeleteStudyGroupUseCase;
import com.nexora.core.application.content.usecases.studygroups.commands.EditStudyGroupUseCase;
import com.nexora.core.application.content.usecases.studygroups.commands.JoinStudyGroupUseCase;
import com.nexora.core.application.content.usecases.studygroups.commands.LeaveStudyGroupUseCase;
import com.nexora.core.application.content.usecases.studygroups.queries.GetMyGroupsUseCase;
import com.nexora.core.application.content.usecases.studygroups.queries.GetStudyGroupBySlugUseCase;
import com.nexora.core.application.content.usecases.studygroups.queries.GetStudyGroupsUseCase;
import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.GroupMembership;
import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.presentation.graphql.dto.CreateStudyGroupInput;
import com.nexora.core.presentation.graphql.dto.UpdateStudyGroupInput;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StudyGroupGraphQlController {

    private final GetStudyGroupsUseCase getStudyGroupsUseCase;
    private final GetStudyGroupBySlugUseCase getStudyGroupBySlugUseCase;
    private final GetMyGroupsUseCase getMyGroupsUseCase;
    private final CreateStudyGroupUseCase createStudyGroupUseCase;
    private final EditStudyGroupUseCase editStudyGroupUseCase;
    private final DeleteStudyGroupUseCase deleteStudyGroupUseCase;
    private final JoinStudyGroupUseCase joinStudyGroupUseCase;
    private final LeaveStudyGroupUseCase leaveStudyGroupUseCase;
    private final ApproveMembershipUseCase approveMembershipUseCase;
    private final SecurityService securityService;

    @QueryMapping
    public List<StudyGroup> studyGroups(@Argument int limit, @Argument int offset, @Argument String category) {
        UUID currentUserId = null;
        try {
            currentUserId = securityService.getCurrentUserId();
        } catch (Exception ignored) {}
        return getStudyGroupsUseCase.execute(limit, offset, category, currentUserId);
    }

    @QueryMapping
    public StudyGroup studyGroupBySlug(@Argument String slug) {
        UUID currentUserId = null;
        try {
            currentUserId = securityService.getCurrentUserId();
        } catch (Exception ignored) {}

        return getStudyGroupBySlugUseCase.execute(slug, currentUserId)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
    }

    @QueryMapping
    public List<StudyGroup> myGroups() {
        UUID userId = securityService.getCurrentUserId();
        return getMyGroupsUseCase.execute(userId);
    }

    @MutationMapping
    public StudyGroup crearGrupo(@Argument CreateStudyGroupInput input) {
        UUID userId = securityService.getCurrentUserId();
        return createStudyGroupUseCase.execute(
                userId, input.name(), input.description(),
                input.category(), input.isPrivate(), input.maxMembers());
    }

    @MutationMapping
    public StudyGroup editarGrupo(@Argument UUID groupId, @Argument UpdateStudyGroupInput input) {
        UUID userId = securityService.getCurrentUserId();
        return editStudyGroupUseCase.execute(
                groupId, userId, input.name(), input.description(),
                input.category(), input.isPrivate(), input.maxMembers());
    }

    @MutationMapping
    public boolean eliminarGrupo(@Argument UUID groupId) {
        UUID userId = securityService.getCurrentUserId();
        return deleteStudyGroupUseCase.execute(groupId, userId);
    }

    @MutationMapping
    public GroupMembership unirseGrupo(@Argument UUID groupId) {
        UUID userId = securityService.getCurrentUserId();
        return joinStudyGroupUseCase.execute(groupId, userId);
    }

    @MutationMapping
    public boolean salirGrupo(@Argument UUID groupId) {
        UUID userId = securityService.getCurrentUserId();
        return leaveStudyGroupUseCase.execute(groupId, userId);
    }

    @MutationMapping
    public GroupMembership aprobarMembresia(@Argument UUID groupId, @Argument UUID membershipId) {
        UUID userId = securityService.getCurrentUserId();
        return approveMembershipUseCase.execute(groupId, membershipId, userId);
    }
}
