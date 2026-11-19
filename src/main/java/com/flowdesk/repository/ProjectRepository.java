
package com.flowdesk.repository;

import com.flowdesk.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrganization_Id(Long organizationId);

    Optional<Project> findByIdAndOrganization_Id(Long id, Long organizationId);
    @Query("""
        SELECT p FROM Project p
        WHERE p.organization.id = :organizationId
        AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))
        )
    """)
    Page<Project> searchProjects(
            @Param("organizationId") Long organizationId,
            @Param("search") String search,
            Pageable pageable
    );

}
