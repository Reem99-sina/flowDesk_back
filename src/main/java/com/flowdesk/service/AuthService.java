package com.flowdesk.service;

import com.flowdesk.dto.auth.RegisterRequest;
import com.flowdesk.dto.auth.RegisterResponse;
import com.flowdesk.dto.auth.OrganizationResponse;
import com.flowdesk.dto.auth.UserResponse;
import com.flowdesk.entity.Organization;
import com.flowdesk.entity.User;
import com.flowdesk.enums.UserRole;
import com.flowdesk.enums.UserStatus;
import com.flowdesk.repository.OrganizationRepository;
import com.flowdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.flowdesk.dto.auth.LoginRequest;
import com.flowdesk.dto.auth.LoginResponse;
import com.flowdesk.exception.InvalidCredentialsException;

import com.flowdesk.exception.DuplicateUserEmailException;

import com.flowdesk.exception.ResourceAlreadyExistsException;
import com.flowdesk.enums.OrganizationStatus;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (organizationRepository.existsByEmail(request.getOrganizationEmail())) {
            throw new ResourceAlreadyExistsException(
                    "Organization email already exists"
            );
        }

        if (organizationRepository.existsBySlug(request.getOrganizationSlug())) {
            throw new ResourceAlreadyExistsException(
                    "Organization slug already exists"
            );
        }

        Organization organization = new Organization();
        organization.setName(request.getOrganizationName());
        organization.setEmail(request.getOrganizationEmail());
        organization.setSlug(request.getOrganizationSlug());

        organization = organizationRepository.save(organization);

        User owner = new User();
        owner.setOrganization(organization);
        owner.setFullName(request.getFullName());
        owner.setEmail(request.getEmail());
        owner.setPassword(passwordEncoder.encode(request.getPassword()));
        owner.setRole(UserRole.OWNER);
        owner.setStatus(UserStatus.ACTIVE);

        owner = userRepository.save(owner);

        OrganizationResponse organizationResponse =
                new OrganizationResponse(
                        organization.getId(),
                        organization.getName(),
                        organization.getSlug(),
                        organization.getEmail(),
                        organization.getStatus()
                );

        UserResponse userResponse = new UserResponse(
                owner.getId(),
                owner.getFullName(),
                owner.getEmail(),
                owner.getRole(),
                owner.getStatus()
        );

        return new RegisterResponse(
                organizationResponse,
                userResponse
        );
    }
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByOrganization_SlugAndEmail(
                        request.getOrganizationSlug(),
                        request.getEmail()
                )
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid email or password"
                ));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException(
                    "Your account is not active"
            );
        }

        if (user.getPassword() == null ||
                !passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                )) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        Organization organization = organizationRepository
                .findById(user.getOrganization().getId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        if (organization.getStatus() != OrganizationStatus.ACTIVE) {
            throw new InvalidCredentialsException(
                    "Organization is not active"
            );
        }

        String token = jwtService.generateToken(user);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );

        OrganizationResponse organizationResponse =
                new OrganizationResponse(
                        organization.getId(),
                        organization.getName(),
                        organization.getSlug(),
                        organization.getEmail(),
                        organization.getStatus()
                );

        return new LoginResponse(
                token,
                "Bearer",
                userResponse,
                organizationResponse
        );
    }
}