
package com.flowdesk.security;

import com.flowdesk.entity.User;
import com.flowdesk.enums.OrganizationStatus;
import com.flowdesk.enums.UserStatus;
import com.flowdesk.repository.UserRepository;
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

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

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
                Long organizationId =
                        jwtService.extractOrganizationId(token);

                User user = userRepository
                        .findByIdAndOrganization_Id(userId, organizationId)
                        .orElse(null);

                if (user == null
                        || user.getStatus() != UserStatus.ACTIVE
                        || user.getOrganization().getStatus()
                                != OrganizationStatus.ACTIVE) {

                    SecurityContextHolder.clearContext();

                } else {

                    String email = user.getEmail();
                    String role = user.getRole().name();

                    UserPrincipalDetails principal =
                            new UserPrincipalDetails(
                                    user.getId(),
                                    user.getOrganization().getId(),
                                    user.getRole()
                            );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    List.of(
                                            new SimpleGrantedAuthority(
                                                    "ROLE_" + role
                                            )
                                    )
                            );

                    authentication.setDetails(email);

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                    System.out.println(
                            "JWT FILTER PRINCIPAL: " +
                            SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getPrincipal()
                                    .getClass()
                                    .getName()
                    );
                }
            }

        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
