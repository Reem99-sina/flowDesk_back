
package com.flowdesk.repository;

import com.flowdesk.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAllByOrganization_Id(Long organizationId);

    Optional<Customer> findByIdAndOrganization_Id(
        Long id,
        Long organizationId
    );

    boolean existsByEmailAndOrganization_Id(
        String email,
        Long organizationId
    );


@Query("""
    SELECT c FROM Customer c
    WHERE c.organization.id = :organizationId
    AND (
        LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%'))
    )
""")
Page<Customer> searchCustomers(
        @Param("organizationId") Long organizationId,
        @Param("search") String search,
        Pageable pageable
);
}
