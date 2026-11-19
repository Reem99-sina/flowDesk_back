package com.flowdesk.dto.auth;

import com.flowdesk.enums.UserRole;
import com.flowdesk.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private UserStatus status;
}