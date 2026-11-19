
package com.flowdesk.repository;

import com.flowdesk.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findBySlug(String slug);

    boolean existsByEmail(String email);

    boolean existsBySlug(String slug);
    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
