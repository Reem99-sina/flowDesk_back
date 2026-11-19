package com.flowdesk.security;

public record UserPrincipalDetails(
        Long userId,
        Long organizationId
) {
}