package com.flowdesk.controller;

import com.flowdesk.dto.response.DashboardSummaryResponse;
import com.flowdesk.security.UserPrincipalDetails;
import com.flowdesk.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final TaskService taskService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            Authentication authentication
    ) {
        UserPrincipalDetails principal =
                (UserPrincipalDetails) authentication.getPrincipal();

        Long assignedToId =
                principal.role().name().equals("AGENT")
                        ? principal.userId()
                        : null;

        return ResponseEntity.ok(
                taskService.getTaskStatistics(
                        principal.organizationId(),
                        assignedToId
                )
        );
    }
}