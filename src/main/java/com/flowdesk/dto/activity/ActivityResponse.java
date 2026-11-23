
package com.flowdesk.dto.activity;

import com.flowdesk.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {

    private Long id;

    private Long userId;

    private String userName;

    private ActivityType type;

    private String description;

    private String targetName;

    private String targetType;

    private Long targetId;

    private LocalDateTime createdAt;
}
