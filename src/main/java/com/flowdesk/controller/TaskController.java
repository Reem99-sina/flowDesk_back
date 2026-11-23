
package com.flowdesk.controller;

import com.flowdesk.dto.request.CreateTaskRequest;
import com.flowdesk.dto.request.UpdateTaskRequest;
import com.flowdesk.dto.request.UpdateTaskStatusRequest;
import com.flowdesk.dto.response.TaskResponse;
import com.flowdesk.enums.TaskStatus;
import com.flowdesk.security.UserPrincipalDetails;
import com.flowdesk.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    private Long getOrganizationId(Authentication authentication) {
        UserPrincipalDetails principal =
                (UserPrincipalDetails) authentication.getPrincipal();

        return principal.organizationId();
    }

    private UserPrincipalDetails getPrincipal(Authentication authentication) {
        return (UserPrincipalDetails) authentication.getPrincipal();
    }

    // Create Task
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication
    ) {
        TaskResponse response = taskService.createTask(
                getPrincipal(authentication).organizationId(),
                getPrincipal(authentication).userId(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get Task By ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.getTaskById(
                        id,
                        getPrincipal(authentication).organizationId()
                )
        );
    }

    // Get Tasks By Project
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ResponseEntity<List<TaskResponse>> getTasksByProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.getTasksByProject(
                        projectId,
                        getPrincipal(authentication).organizationId()
                )
        );
    }

    // Update Task
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                taskService.updateTask(
                        id,
                        getPrincipal(authentication).organizationId(),
                        getPrincipal(authentication).userId(),
                        request
                )
        );
    }

    // Delete Task
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            Authentication authentication
    ) {
        taskService.deleteTask(
                id,
                getPrincipal(authentication).organizationId()
        );

        return ResponseEntity.noContent().build();
    }

    // Get My Tasks
    @GetMapping("/my-tasks")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<List<TaskResponse>> getMyTasks(
            Authentication authentication
    ) {
        UserPrincipalDetails principal = getPrincipal(authentication);

        return ResponseEntity.ok(
                taskService.getMyTasks(
                        principal.userId(),
                        principal.organizationId()
                )
        );
    }

    // Update My Task Status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<TaskResponse> updateMyTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            Authentication authentication
    ) {
        UserPrincipalDetails principal = getPrincipal(authentication);

        System.out.println("Agent User ID: " + principal.userId());
        System.out.println("Organization ID: " + principal.organizationId());
        System.out.println("Task ID: " + id);

        return ResponseEntity.ok(
                taskService.updateMyTaskStatus(
                        id,
                        principal.userId(),
                        principal.organizationId(),
                        request.getStatus()
                )
        );
    }

    // Get Tasks With Search, Status Filter and Pagination
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(
                    page = 0,
                    size = 5,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            Authentication authentication
    ) {
        Long organizationId = getOrganizationId(authentication);

        return ResponseEntity.ok(
                taskService.searchTasks(
                        organizationId,
                        search,
                        status,
                        pageable
                )
        );
    }
}
