
package com.flowdesk.dto.project;

import com.flowdesk.enums.ProjectStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateProjectRequest {

    @Size(max = 150)
    private String name;

    private String description;

    private Long customerId;

    private ProjectStatus status;

    private LocalDate startDate;

    private LocalDate dueDate;

    @DecimalMin(value = "0.0", message = "Budget cannot be negative")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal budget;
}
