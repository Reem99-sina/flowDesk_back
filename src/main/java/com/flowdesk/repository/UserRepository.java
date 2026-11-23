package com.flowdesk.repository;

import com.flowdesk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import com.flowdesk.enums.UserRole;
import com.flowdesk.enums.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOrganization_IdAndEmail(Long organizationId, String email);

    @EntityGraph(attributePaths = "organization")
    Optional<User> findByIdAndOrganization_Id(
            Long id,
            Long organizationId
    );

    boolean existsByOrganization_IdAndEmail(
            Long organizationId,
            String email
    );
    Optional<User> findByOrganization_SlugAndEmail(
            String organizationSlug,
            String email
    );
    List<User> findAllByOrganization_Id(Long organizationId);

    List<User> findAllByOrganization_IdAndRoleAndStatus(
            Long organizationId,
            UserRole role,
            UserStatus status
    );
}