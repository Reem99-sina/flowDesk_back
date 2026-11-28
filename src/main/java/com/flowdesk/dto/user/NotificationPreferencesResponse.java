
package com.flowdesk.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationPreferencesResponse {

    private Boolean emailNotifications;

    private Boolean taskAssignmentNotifications;

    private Boolean taskUpdateNotifications;

    private Boolean deadlineReminderNotifications;
}
