package com.flowdesk.controller;

import com.flowdesk.dto.project.CreateProjectRequest;
import com.flowdesk.dto.project.ProjectResponse;
import com.flowdesk.security.UserPrincipalDetails;
import com.flowdesk.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.flowdesk.dto.project.UpdateProjectRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ProjectResponse createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication
    ) {
        UserPrincipalDetails principal =
                (UserPrincipalDetails) authentication.getDetails();

        return projectService.createProject(
                principal.organizationId(),
                request
        );
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public List<ProjectResponse> getAllProjects(
            Authentication authentication
    ) {
        UserPrincipalDetails principal =
                (UserPrincipalDetails) authentication.getDetails();

        return projectService.getAllProjects(principal.organizationId());
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ProjectResponse getProjectById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UserPrincipalDetails principal =
                (UserPrincipalDetails) authentication.getDetails();

        return projectService.getProjectById(
                id,
                principal.organizationId()
        );
    }

@PatchMapping("/{id}")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public ProjectResponse updateProject(
        @PathVariable Long id,
        @Valid @RequestBody UpdateProjectRequest request,
        Authentication authentication
) {
    UserPrincipalDetails principal =
            (UserPrincipalDetails) authentication.getDetails();

    return projectService.updateProject(
            id,
            principal.organizationId(),
            request
    );
}

@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public void deleteProject(
        @PathVariable Long id,
        Authentication authentication
) {
    UserPrincipalDetails principal =
            (UserPrincipalDetails) authentication.getDetails();

    projectService.deleteProject(
            id,
            principal.organizationId()
    );
}

@GetMapping("/search")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
public Page<ProjectResponse> searchProjects(
        @RequestParam(required = false) String search,
        @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC)
        Pageable pageable,
        Authentication authentication
) {
    UserPrincipalDetails principal =
            (UserPrincipalDetails) authentication.getDetails();

    return projectService.searchProjects(
            principal.organizationId(),
            search,
            pageable
    );
}

}
