package com.flowdesk.dto.request;

import com.flowdesk.enums.TaskPriority;
import com.flowdesk.enums.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateTaskRequest {

    @Size(max = 150)
    private String title;

    private String description;

    private Long projectId;

    private Long assignedTo;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate startDate;

    private LocalDate dueDate;
}