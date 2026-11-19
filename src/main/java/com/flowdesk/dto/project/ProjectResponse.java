
package com.flowdesk.dto.project;

import com.flowdesk.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectResponse {

    private Long id;

    private String name;

    private String description;

    private Long customerId;

    private String customerName;

    private ProjectStatus status;

    private LocalDate startDate;

    private LocalDate dueDate;

    private BigDecimal budget;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
