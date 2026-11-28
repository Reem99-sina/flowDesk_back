
package com.flowdesk.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponse {

    private Long id;

    private String title;

    private String message;

    private String type;

    private Boolean isRead;

    private LocalDateTime createdAt;
}
