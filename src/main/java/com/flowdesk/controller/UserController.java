package com.flowdesk.controller;

import com.flowdesk.dto.auth.CreateUserRequest;
import com.flowdesk.dto.auth.UpdateUserRequest;
import com.flowdesk.dto.auth.UserResponse;
import com.flowdesk.dto.response.ApiResponse;
import com.flowdesk.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.flowdesk.security.UserPrincipalDetails;
import java.util.List;
import com.flowdesk.dto.auth.UserStatusUpdateRequest;
import com.flowdesk.service.InvitationService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final InvitationService invitationService;
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication
    ) {
        UserPrincipalDetails principalDetails =
                (UserPrincipalDetails) authentication.getDetails();

        Long organizationId = principalDetails.organizationId();

        String currentRole = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");

        UserResponse user = userService.createUser(
                organizationId,
                request,
                currentRole
        );

        ApiResponse<UserResponse> response = new ApiResponse<>(
                true,
                "User created successfully",
                user
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            Authentication authentication
    ) {
        UserPrincipalDetails principalDetails =
                (UserPrincipalDetails) authentication.getDetails();

        Long organizationId = principalDetails.organizationId();

        List<UserResponse> users = userService.getAllUsers(organizationId);

        ApiResponse<List<UserResponse>> response = new ApiResponse<>(
                true,
                "Users retrieved successfully",
                users
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UserPrincipalDetails principalDetails =
                (UserPrincipalDetails) authentication.getDetails();

        Long organizationId = principalDetails.organizationId();

        UserResponse user = userService.getUserById(id, organizationId);

        ApiResponse<UserResponse> response = new ApiResponse<>(
                true,
                "User retrieved successfully",
                user
        );

        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        UserPrincipalDetails principalDetails =
                (UserPrincipalDetails) authentication.getDetails();

        Long organizationId = principalDetails.organizationId();

        String currentRole = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");
        Long currentUserId = principalDetails.userId();

        UserResponse user = userService.updateUser(
                id,
                organizationId,
                request,
                currentRole,
                currentUserId
        );

        ApiResponse<UserResponse> response = new ApiResponse<>(
                true,
                "User updated successfully",
                user
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            Authentication authentication
    ) {
        UserPrincipalDetails principalDetails =
                (UserPrincipalDetails) authentication.getDetails();

        Long organizationId = principalDetails.organizationId();

        String currentRole = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");

        UserResponse user = userService.updateUserStatus(
                id,
                organizationId,
                request,
                currentRole
        );

        ApiResponse<UserResponse> response = new ApiResponse<>(
                true,
                "User status updated successfully",
                user
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UserPrincipalDetails principalDetails =
                (UserPrincipalDetails) authentication.getDetails();

        Long organizationId = principalDetails.organizationId();

        String currentRole = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");
        Long currentUserId = principalDetails.userId();

        userService.deleteUser(
                id,
                organizationId,
                currentRole,
                currentUserId
        );

        ApiResponse<Void> response = new ApiResponse<>(
                true,
                "User deleted successfully",
                null
        );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/resend-invitation")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> resendInvitation(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UserPrincipalDetails principalDetails =
                (UserPrincipalDetails) authentication.getDetails();

        Long organizationId = principalDetails.organizationId();

        invitationService.resendInvitation(id, organizationId);

        ApiResponse<Void> response = new ApiResponse<>(
                true,
                "Invitation resent successfully",
                null
        );

        return ResponseEntity.ok(response);
    }

}