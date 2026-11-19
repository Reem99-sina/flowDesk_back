package com.flowdesk.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 150)
    private String organizationName;

    @NotBlank(message = "Organization email is required")
    @Email(message = "Invalid organization email")
    @Size(max = 255)
    private String organizationEmail;

    @NotBlank(message = "Organization slug is required")
    @Size(max = 100)
    private String organizationSlug;

    @NotBlank(message = "Full name is required")
    @Size(max = 150)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;
}