package com.flowdesk.dto.auth;

import com.flowdesk.enums.OrganizationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationStatusUpdateRequest {

    @NotNull(message = "Organization status is required")
    private OrganizationStatus status;
}