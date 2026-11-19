
package com.flowdesk.dto.customer;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String company;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
