
package com.flowdesk.controller;

import com.flowdesk.dto.response.ApiResponse;
import com.flowdesk.dto.auth.OrganizationResponse;
import com.flowdesk.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import com.flowdesk.dto.auth.OrganizationUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.flowdesk.dto.auth.OrganizationStatusUpdateRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import com.flowdesk.security.UserPrincipalDetails;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<ApiResponse<OrganizationResponse>> getCurrentOrganization(
            Authentication authentication) {
UserPrincipalDetails principalDetails =
        (UserPrincipalDetails) authentication.getDetails();

Long organizationId = principalDetails.organizationId();

        OrganizationResponse data =
                organizationService.getOrganizationById(organizationId);

        ApiResponse<OrganizationResponse> response =
                new ApiResponse<>(true, "Organization retrieved successfully", data);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        UserPrincipalDetails principalDetails =
                (UserPrincipalDetails) authentication.getDetails();

        Long currentOrganizationId = principalDetails.organizationId();

        OrganizationResponse data = organizationService.getOrganizationById(
                id,
                currentOrganizationId
        );

        ApiResponse<OrganizationResponse> response =
                new ApiResponse<>(
                        true,
                        "Organization retrieved successfully",
                        data
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @Valid @RequestBody OrganizationUpdateRequest request,
            Authentication authentication
    ) {
        UserPrincipalDetails principalDetails =
                (UserPrincipalDetails) authentication.getDetails();

        Long organizationId = principalDetails.organizationId();

        OrganizationResponse data = organizationService.updateOrganization(
                organizationId,
                request
        );

        ApiResponse<OrganizationResponse> response = new ApiResponse<>(
                true,
                "Organization updated successfully",
                data
        );

        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganizationStatus(
            @Valid @RequestBody OrganizationStatusUpdateRequest request,
            Authentication authentication
    ) {
        UserPrincipalDetails principalDetails =
                (UserPrincipalDetails) authentication.getDetails();

        Long organizationId = principalDetails.organizationId();

        OrganizationResponse organization =
                organizationService.updateOrganizationStatus(
                        organizationId,
                        request
                );

        ApiResponse<OrganizationResponse> response = new ApiResponse<>(
                true,
                "Organization status updated successfully",
                organization
        );

        return ResponseEntity.ok(response);
    }

}
