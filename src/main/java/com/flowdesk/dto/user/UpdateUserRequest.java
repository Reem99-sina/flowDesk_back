
package com.flowdesk.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @Size(
        min = 2,
        max = 100,
        message = "Full name must be between 2 and 100 characters"
    )
    private String fullName;

    @Email(message = "Invalid email")
    @Size(
        min = 5,
        max = 150,
        message = "Email must be between 5 and 150 characters"
    )
    private String email;
}
