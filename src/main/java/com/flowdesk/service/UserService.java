package com.flowdesk.service;

import com.flowdesk.repository.UserRepository;
import com.flowdesk.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.flowdesk.dto.auth.CreateUserRequest;
import com.flowdesk.dto.auth.UserResponse;
import com.flowdesk.entity.Organization;
import com.flowdesk.entity.User;
import com.flowdesk.enums.UserRole;
import com.flowdesk.enums.UserStatus;
import com.flowdesk.exception.DuplicateUserEmailException;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import com.flowdesk.dto.auth.UpdateUserRequest;
import com.flowdesk.dto.auth.UserStatusUpdateRequest;
import com.flowdesk.dto.auth.ChangePasswordRequest;
import com.flowdesk.dto.user.NotificationPreferencesRequest;
import com.flowdesk.dto.user.NotificationPreferencesResponse;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvitationService invitationService;
    public UserResponse createUser(
            Long organizationId,
            Long currentUserId,
            CreateUserRequest request,
            String currentRole
    ){
        if (currentRole.equals("ADMIN")
                && request.getRole() != UserRole.AGENT) {
            throw new AccessDeniedException(
                    "Admin can only create agents"
            );
        }

        if (currentRole.equals("OWNER")
                && request.getRole() == UserRole.OWNER) {
            throw new AccessDeniedException(
                    "Cannot create another owner"
            );
        }

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organization not found"
                ));

        if (userRepository.existsByOrganization_IdAndEmail(
                organizationId,
                request.getEmail()
        )) {
            throw new DuplicateUserEmailException(
                    "User email already exists in this organization"
            );
        }

        User user = new User();
        user.setOrganization(organization);
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        user.setRole(request.getRole());
        user.setPassword(null);
        user.setStatus(UserStatus.INVITED);

        user = userRepository.save(user);
        User invitedBy = userRepository
                .findByIdAndOrganization_Id(currentUserId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Inviting user not found"));

        invitationService.sendInvitation(user, invitedBy);

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(Long organizationId) {

        List<User> users = userRepository
                .findAllByOrganization_Id(organizationId);

        return users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus()
                ))
                .toList();
    }
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId, Long organizationId) {
        User user = userRepository
                .findByIdAndOrganization_Id(userId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }
    @Transactional
    public UserResponse updateUser(
            Long userId,
            Long organizationId,
            UpdateUserRequest request,
            String currentRole,
            Long currentUserId
    ){
        if (request.getFullName() == null && request.getEmail() == null) {
            throw new IllegalArgumentException("At least one field is required for update");
        }

        User user = userRepository
                .findByIdAndOrganization_Id(userId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (currentRole.equals("ADMIN")
                && user.getRole() != UserRole.AGENT
                && !user.getId().equals(currentUserId)) {

            throw new AccessDeniedException(
                    "Admin can only update agents or their own account"
            );
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null) {
            if (userRepository.existsByOrganization_IdAndEmail(organizationId, request.getEmail())
                    && !user.getEmail().equals(request.getEmail())) {
                throw new DuplicateUserEmailException("User email already exists in this organization");
            }
            user.setEmail(request.getEmail());
        }

        user = userRepository.save(user);

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    @Transactional
    public UserResponse updateUserStatus(
            Long userId,
            Long organizationId,
            UserStatusUpdateRequest request,
            String currentRole
    ) {
        if (!currentRole.equals("OWNER")) {
            throw new AccessDeniedException(
                    "Only organization owner can update user status"
            );
        }

        User user = userRepository
                .findByIdAndOrganization_Id(userId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == UserRole.OWNER) {
            throw new AccessDeniedException(
                    "Cannot update organization owner's status"
            );
        }

        user.setStatus(request.getStatus());

        user = userRepository.save(user);

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }
@Transactional
public void changePassword(
        Long userId,
        Long organizationId,
        ChangePasswordRequest request
) {
    User user = userRepository
            .findByIdAndOrganization_Id(userId, organizationId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!passwordEncoder.matches(
            request.getCurrentPassword(),
            user.getPassword()
    )) {
        throw new IllegalArgumentException("Current password is incorrect");
    }

    if (passwordEncoder.matches(
            request.getNewPassword(),
            user.getPassword()
    )) {
        throw new IllegalArgumentException(
                "New password must be different from current password"
        );
    }

    user.setPassword(
            passwordEncoder.encode(request.getNewPassword())
    );

    userRepository.save(user);
}

@Transactional
public void deleteUser(
        Long userId,
        Long organizationId,
        String currentRole,
        Long currentUserId
) {
    if (!currentRole.equals("OWNER") && !currentRole.equals("ADMIN")) {
        throw new AccessDeniedException("You are not allowed to delete users");
    }

    if (userId.equals(currentUserId)) {
        throw new AccessDeniedException("You cannot delete your own account");
    }

    User user = userRepository
            .findByIdAndOrganization_Id(userId, organizationId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (user.getRole() == UserRole.OWNER) {
        throw new AccessDeniedException("Cannot delete organization owner");
    }

    if (currentRole.equals("ADMIN") && user.getRole() != UserRole.AGENT) {
        throw new AccessDeniedException("Admin can only delete agents");
    }

    userRepository.delete(user);
}
@Transactional(readOnly = true)
public List<UserResponse> getActiveAgents(Long organizationId) {

    System.out.println("========== ACTIVE AGENTS ==========");
    System.out.println("Organization ID: " + organizationId);

    List<User> agents = userRepository
            .findAllByOrganization_IdAndRoleAndStatus(
                    organizationId,
                    UserRole.AGENT,
                    UserStatus.ACTIVE
            );

    System.out.println("Agents count: " + agents.size());

    agents.forEach(agent ->
            System.out.println(
                    agent.getId() + " | " +
                    agent.getFullName() + " | " +
                    agent.getRole() + " | " +
                    agent.getStatus()
            )
    );

    System.out.println("===================================");

    return agents.stream()
            .map(user -> new UserResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatus()
            ))
            .toList();
}

@Transactional(readOnly = true)
public NotificationPreferencesResponse getNotificationPreferences(
        Long userId,
        Long organizationId
) {
    User user = userRepository
            .findByIdAndOrganization_Id(userId, organizationId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    return new NotificationPreferencesResponse(
            user.getEmailNotifications(),
            user.getTaskAssignmentNotifications(),
            user.getTaskUpdateNotifications(),
            user.getDeadlineReminderNotifications()
    );
}

@Transactional
public NotificationPreferencesResponse updateNotificationPreferences(
        Long userId,
        Long organizationId,
        NotificationPreferencesRequest request
) {
    User user = userRepository
            .findByIdAndOrganization_Id(userId, organizationId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    user.setEmailNotifications(request.getEmailNotifications());
    user.setTaskAssignmentNotifications(request.getTaskAssignmentNotifications());
    user.setTaskUpdateNotifications(request.getTaskUpdateNotifications());
    user.setDeadlineReminderNotifications(request.getDeadlineReminderNotifications());

    user = userRepository.save(user);

    return new NotificationPreferencesResponse(
            user.getEmailNotifications(),
            user.getTaskAssignmentNotifications(),
            user.getTaskUpdateNotifications(),
            user.getDeadlineReminderNotifications()
    );
}

}