
package com.flowdesk.service;

import com.flowdesk.dto.customer.CreateCustomerRequest;
import com.flowdesk.dto.customer.CustomerResponse;
import com.flowdesk.entity.Customer;
import com.flowdesk.entity.Organization;
import com.flowdesk.repository.CustomerRepository;
import com.flowdesk.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flowdesk.dto.customer.UpdateCustomerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public CustomerResponse createCustomer(
            Long organizationId,
            CreateCustomerRequest request
    ) {
        if (request.getEmail() != null &&
                customerRepository.existsByEmailAndOrganization_Id(
                        request.getEmail(), organizationId)) {
            throw new IllegalArgumentException(
                    "Customer email already exists"
            );
        }

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organization not found"
                ));

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCompany(request.getCompany());
        customer.setOrganization(organization);

        Customer savedCustomer = customerRepository.save(customer);

        return mapToResponse(savedCustomer);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .company(customer.getCompany())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

@Transactional(readOnly = true)
public List<CustomerResponse> getAllCustomers(Long organizationId) {

    return customerRepository
            .findAllByOrganization_Id(organizationId)
            .stream()
            .map(this::mapToResponse)
            .toList();
}

@Transactional(readOnly = true)
public CustomerResponse getCustomerById(
        Long customerId,
        Long organizationId
) {
    Customer customer = customerRepository
            .findByIdAndOrganization_Id(customerId, organizationId)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Customer not found"
            ));

    return mapToResponse(customer);
}


@Transactional
public CustomerResponse updateCustomer(
        Long customerId,
        Long organizationId,
        UpdateCustomerRequest request
) {
    Customer customer = customerRepository
            .findByIdAndOrganization_Id(customerId, organizationId)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Customer not found"
            ));

    if (request.getName() != null) {
        customer.setName(request.getName());
    }

    if (request.getEmail() != null) {
        if (!request.getEmail().equals(customer.getEmail()) &&
                customerRepository.existsByEmailAndOrganization_Id(
                        request.getEmail(), organizationId)) {
            throw new IllegalArgumentException(
                    "Customer email already exists"
            );
        }

        customer.setEmail(request.getEmail());
    }

    if (request.getPhone() != null) {
        customer.setPhone(request.getPhone());
    }

    if (request.getCompany() != null) {
        customer.setCompany(request.getCompany());
    }

    Customer updatedCustomer = customerRepository.save(customer);

    return mapToResponse(updatedCustomer);
}


@Transactional
public void deleteCustomer(Long customerId, Long organizationId) {

    Customer customer = customerRepository
            .findByIdAndOrganization_Id(customerId, organizationId)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Customer not found"
            ));

    customerRepository.delete(customer);
}


@Transactional(readOnly = true)
public Page<CustomerResponse> searchCustomers(
        Long organizationId,
        String search,
        Pageable pageable
) {
    String searchTerm = search == null ? "" : search.trim();

    return customerRepository
            .searchCustomers(organizationId, searchTerm, pageable)
            .map(this::mapToResponse);
}



}
