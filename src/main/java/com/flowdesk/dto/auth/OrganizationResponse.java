package com.flowdesk.dto.auth;

import com.flowdesk.enums.OrganizationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrganizationResponse {

    private Long id;
    private String name;
    private String slug;
    private String email;
    private OrganizationStatus status;
}