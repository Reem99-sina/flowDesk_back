
package com.flowdesk.controller;

import com.flowdesk.dto.activity.ActivityResponse;
import com.flowdesk.security.UserPrincipalDetails;
import com.flowdesk.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
    public ResponseEntity<List<ActivityResponse>> getRecentActivities(
            Authentication authentication
    ) {

        UserPrincipalDetails principal =
                (UserPrincipalDetails) authentication.getPrincipal();

        List<ActivityResponse> activities =
                activityService.getRecentActivities(
                        principal.organizationId()
                );

        return ResponseEntity.ok(activities);
    }
}
