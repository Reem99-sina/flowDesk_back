
package com.flowdesk.dto.auth;

import com.flowdesk.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStatusUpdateRequest {

    @NotNull(message = "User status is required")
    private UserStatus status;
}
