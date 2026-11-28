
package com.flowdesk.controller;

import com.flowdesk.dto.response.ApiResponse;
import com.flowdesk.dto.notification.NotificationResponse;
import com.flowdesk.dto.notification.UnreadCountResponse;
import com.flowdesk.security.UserPrincipalDetails;
import com.flowdesk.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>>
    getNotifications(Authentication authentication) {

        UserPrincipalDetails principal =
                (UserPrincipalDetails) authentication.getPrincipal();

        List<NotificationResponse> notifications =
                notificationService.getNotifications(
                        principal.userId()
                );

        ApiResponse<List<NotificationResponse>> response =
                new ApiResponse<>(
                        true,
                        "Notifications retrieved successfully",
                        notifications
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UnreadCountResponse>>
    getUnreadCount(Authentication authentication) {

        UserPrincipalDetails principal =
                (UserPrincipalDetails) authentication.getPrincipal();

        UnreadCountResponse unreadCount =
                notificationService.getUnreadCount(
                        principal.userId()
                );

        ApiResponse<UnreadCountResponse> response =
                new ApiResponse<>(
                        true,
                        "Unread count retrieved successfully",
                        unreadCount
                );

        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {

        UserPrincipalDetails principal =
                (UserPrincipalDetails) authentication.getPrincipal();

        notificationService.markAsRead(
                notificationId,
                principal.userId()
        );

        ApiResponse<Void> response =
                new ApiResponse<>(
                        true,
                        "Notification marked as read successfully",
                        null
                );

        return ResponseEntity.ok(response);
    }
}
