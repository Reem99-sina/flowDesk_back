package com.flowdesk.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterResponse {

    private OrganizationResponse organization;
    private UserResponse user;
}