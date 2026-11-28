package com.flowdesk.security;

import com.flowdesk.enums.UserRole;

public record UserPrincipalDetails(
        Long userId,
        Long organizationId,
        UserRole role
) {
}
