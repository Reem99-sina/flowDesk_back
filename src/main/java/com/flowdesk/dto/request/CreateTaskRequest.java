package com.flowdesk.dto.request;

import com.flowdesk.enums.TaskPriority;
import com.flowdesk.enums.TaskStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 150)
    private String title;

    private String description;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assignedTo;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate startDate;

    private LocalDate dueDate;
}