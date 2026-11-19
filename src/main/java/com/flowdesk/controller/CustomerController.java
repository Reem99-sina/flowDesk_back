package com.flowdesk.controller;

import com.flowdesk.dto.customer.CreateCustomerRequest;
import com.flowdesk.dto.customer.CustomerResponse;
import com.flowdesk.security.UserPrincipalDetails;
import com.flowdesk.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.flowdesk.dto.customer.UpdateCustomerRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public CustomerResponse createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            Authentication authentication
    ) {
        UserPrincipalDetails principal =
                (UserPrincipalDetails) authentication.getDetails();

        return customerService.createCustomer(
                principal.organizationId(),
                request
        );
    }

@GetMapping
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
public List<CustomerResponse> getAllCustomers(
        Authentication authentication
) {
    UserPrincipalDetails principal =
            (UserPrincipalDetails) authentication.getDetails();

    return customerService.getAllCustomers(
            principal.organizationId()
    );
}

@GetMapping("/{id}")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
public CustomerResponse getCustomerById(
        @PathVariable Long id,
        Authentication authentication
) {
    UserPrincipalDetails principal =
            (UserPrincipalDetails) authentication.getDetails();

    return customerService.getCustomerById(
            id,
            principal.organizationId()
    );
}

@PatchMapping("/{id}")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public CustomerResponse updateCustomer(
        @PathVariable Long id,
        @Valid @RequestBody UpdateCustomerRequest request,
        Authentication authentication
) {
    UserPrincipalDetails principal =
            (UserPrincipalDetails) authentication.getDetails();

    return customerService.updateCustomer(
            id,
            principal.organizationId(),
            request
    );
}

@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public void deleteCustomer(
        @PathVariable Long id,
        Authentication authentication
) {
    UserPrincipalDetails principal =
            (UserPrincipalDetails) authentication.getDetails();

    customerService.deleteCustomer(
            id,
            principal.organizationId()
    );
}

@GetMapping("/search")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'AGENT')")
public Page<CustomerResponse> searchCustomers(
        @RequestParam(required = false) String search,
        @PageableDefault(size = 10, sort = "name") Pageable pageable,
        Authentication authentication
) {
    UserPrincipalDetails principal =
            (UserPrincipalDetails) authentication.getDetails();

    return customerService.searchCustomers(
            principal.organizationId(),
            search,
            pageable
    );
}


}
