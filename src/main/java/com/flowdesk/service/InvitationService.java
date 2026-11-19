package com.flowdesk.service;

import com.flowdesk.entity.Invitation;
import com.flowdesk.entity.User;
import com.flowdesk.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import com.flowdesk.dto.auth.AcceptInvitationRequest;
import com.flowdesk.enums.UserStatus;
import com.flowdesk.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final InvitationTokenService tokenService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public void sendInvitation(User user) {

        String token = tokenService.generateToken();

        String tokenHash = tokenService.hashToken(token);

        Invitation invitation = new Invitation();
        invitation.setUser(user);
        invitation.setTokenHash(tokenHash);
        invitation.setExpiresAt(
                LocalDateTime.now().plusHours(24)
        );

        invitationRepository.save(invitation);

        String invitationLink = frontendUrl
                + "/accept-invitation?token="
                + token;

        String subject = "You're invited to join FlowDesk";

        String body = """
                Hello %s,

                You have been invited to join FlowDesk.

                Click the link below to accept your invitation
                and set your password:

                %s

                This invitation expires in 24 hours.

                If you were not expecting this invitation,
                you can ignore this email.
                """.formatted(
                user.getFullName(),
                invitationLink
        );

        emailService.sendEmail(
                user.getEmail(),
                subject,
                body
        );
    }
    @Transactional
    public void acceptInvitation(AcceptInvitationRequest request) {

        String tokenHash = tokenService.hashToken(request.getToken());

        Invitation invitation = invitationRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid invitation token"
                ));

        if (invitation.getUsedAt() != null) {
            throw new IllegalArgumentException(
                    "Invitation has already been used"
            );
        }

        if (!invitation.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Invitation has expired"
            );
        }

        User user = invitation.getUser();

        if (user.getStatus() != UserStatus.INVITED) {
            throw new IllegalArgumentException(
                    "User is not in invited status"
            );
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        invitation.setUsedAt(LocalDateTime.now());

        invitationRepository.save(invitation);
    }
    @Transactional
    public void resendInvitation(Long userId, Long organizationId) {

        User user = userRepository
                .findByIdAndOrganization_Id(userId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"
                ));

        if (user.getStatus() != UserStatus.INVITED) {
            throw new IllegalArgumentException(
                    "Invitation can only be resent to invited users"
            );
        }

        List<Invitation> invitations =
                invitationRepository.findByUserId(userId);

        for (Invitation invitation : invitations) {
            if (invitation.getUsedAt() == null) {
                invitation.setExpiresAt(LocalDateTime.now());
            }
        }

        invitationRepository.saveAll(invitations);

        sendInvitation(user);
    }
}