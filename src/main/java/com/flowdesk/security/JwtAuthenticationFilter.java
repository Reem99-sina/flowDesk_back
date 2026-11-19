
package com.flowdesk.security;

import com.flowdesk.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import com.flowdesk.entity.User;
import com.flowdesk.enums.UserStatus;
import com.flowdesk.enums.OrganizationStatus;
import com.flowdesk.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

if (jwtService.isTokenValid(token)) {

    Long userId = jwtService.extractUserId(token);
    Long organizationId = jwtService.extractOrganizationId(token);

    User user = userRepository
            .findByIdAndOrganization_Id(userId, organizationId)
            .orElse(null);

    if (user == null ||
            user.getStatus() != UserStatus.ACTIVE ||
            user.getOrganization().getStatus() != OrganizationStatus.ACTIVE) {

        SecurityContextHolder.clearContext();

    } else {

        String email = user.getEmail();
        String role = user.getRole().name();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        authentication.setDetails(
                new UserPrincipalDetails(
                        user.getId(),
                        user.getOrganization().getId()
                )
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}


        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
