package com.flowdesk.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class OrganizationUpdateRequest {

    @Pattern(regexp = ".*\\S.*", message = "Name cannot be blank")
    @Size(min = 2, max = 150)
    private String name;

    @Pattern(regexp = ".*\\S.*", message = "Slug cannot be blank")
    @Size(min = 3, max = 100)
    private String slug;

    @Pattern(regexp = ".*\\S.*", message = "Email cannot be blank")
    @Email
    @Size(max = 255)
    private String email;
}