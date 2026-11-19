package com.flowdesk.dto.response;

import com.flowdesk.enums.TaskPriority;
import com.flowdesk.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private Long projectId;

    private String projectName;

    private Long assignedTo;

    private String assignedToName;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate startDate;

    private LocalDate dueDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}