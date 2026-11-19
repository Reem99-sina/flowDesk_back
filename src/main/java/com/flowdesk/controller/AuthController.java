package com.flowdesk.controller;

import com.flowdesk.dto.auth.RegisterRequest;
import com.flowdesk.dto.auth.RegisterResponse;
import com.flowdesk.dto.response.ApiResponse;
import com.flowdesk.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.flowdesk.dto.auth.LoginRequest;
import com.flowdesk.dto.auth.LoginResponse;
import com.flowdesk.dto.auth.AcceptInvitationRequest;
import com.flowdesk.service.InvitationService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final InvitationService invitationService;

    @PostMapping("/register")
     public ResponseEntity<ApiResponse<RegisterResponse>> register(
             @Valid @RequestBody RegisterRequest request
     ) {
         RegisterResponse data = authService.register(request);

         ApiResponse<RegisterResponse> response = new ApiResponse<>(
                 true,
                 "Organization registered successfully",
                 data
         );

         return ResponseEntity
                 .status(HttpStatus.CREATED)
                 .body(response);
     }
     @PostMapping("/login")
     public ResponseEntity<ApiResponse<LoginResponse>> login(
             @Valid @RequestBody LoginRequest request
     ) {
         LoginResponse data = authService.login(request);

         ApiResponse<LoginResponse> response = new ApiResponse<>(
                 true,
                 "Login successful",
                 data
         );

         return ResponseEntity.ok(response);
     }
     @PostMapping("/accept-invitation")
     public ResponseEntity<ApiResponse<Void>> acceptInvitation(
             @Valid @RequestBody AcceptInvitationRequest request
     ) {

         invitationService.acceptInvitation(request);

         ApiResponse<Void> response = new ApiResponse<>(
                 true,
                 "Invitation accepted successfully. Your account is now active.",
                 null
         );

         return ResponseEntity.ok(response);
     }
}