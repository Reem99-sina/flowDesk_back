
package com.flowdesk.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationPreferencesRequest {

    @NotNull
    private Boolean emailNotifications;

    @NotNull
    private Boolean taskAssignmentNotifications;

    @NotNull
    private Boolean taskUpdateNotifications;

    @NotNull
    private Boolean deadlineReminderNotifications;
}
