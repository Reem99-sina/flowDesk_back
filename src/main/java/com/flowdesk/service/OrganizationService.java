
package com.flowdesk.service;

import com.flowdesk.dto.auth.OrganizationResponse;
import com.flowdesk.entity.Organization;
import com.flowdesk.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.flowdesk.dto.auth.OrganizationUpdateRequest;
import com.flowdesk.exception.ResourceAlreadyExistsException;
import com.flowdesk.dto.auth.OrganizationStatusUpdateRequest;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(Long organizationId) {

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Organization not found")
                );

        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                organization.getEmail(),
                organization.getStatus()
        );
    }
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(
            Long organizationId,
            Long currentOrganizationId
    ) {
        if (!organizationId.equals(currentOrganizationId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You cannot access another organization"
            );
        }

        return getOrganizationById(organizationId);
    }



@Transactional
    public OrganizationResponse updateOrganization(
            Long organizationId,
            OrganizationUpdateRequest request
    ) {
    if (request.getName() != null && request.getName().isBlank()) {
        throw new IllegalArgumentException("Organization name cannot be blank");
    }

    if (request.getSlug() != null && request.getSlug().isBlank()) {
        throw new IllegalArgumentException("Organization slug cannot be blank");
    }

    if (request.getEmail() != null && request.getEmail().isBlank()) {
        throw new IllegalArgumentException("Organization email cannot be blank");
    }
        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Organization not found")
                );

        if (request.getName() != null) {
            organization.setName(request.getName().trim());
        }

        if (request.getSlug() != null) {

            if (organizationRepository.existsBySlugAndIdNot(
                    request.getSlug(), organizationId
            )) {
                throw new ResourceAlreadyExistsException(
                        "Organization slug already exists"
                );
            }

            organization.setSlug(request.getSlug().trim());
        }

        if (request.getEmail() != null) {

            if (organizationRepository.existsByEmailAndIdNot(
                    request.getEmail(), organizationId
            )) {
                throw new ResourceAlreadyExistsException(
                        "Organization email already exists"
                );
            }

            organization.setEmail(request.getEmail().trim());
        }

        Organization updatedOrganization =
                organizationRepository.save(organization);

        return new OrganizationResponse(
                updatedOrganization.getId(),
                updatedOrganization.getName(),
                updatedOrganization.getSlug(),
                updatedOrganization.getEmail(),
                updatedOrganization.getStatus()
        );
    }
    @Transactional
    public OrganizationResponse updateOrganizationStatus(
            Long organizationId,
            OrganizationStatusUpdateRequest request
    ) {
        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Organization not found")
                );

        organization.setStatus(request.getStatus());

        Organization updatedOrganization =
                organizationRepository.save(organization);

        return new OrganizationResponse(
                updatedOrganization.getId(),
                updatedOrganization.getName(),
                updatedOrganization.getSlug(),
                updatedOrganization.getEmail(),
                updatedOrganization.getStatus()
        );
    }
    }