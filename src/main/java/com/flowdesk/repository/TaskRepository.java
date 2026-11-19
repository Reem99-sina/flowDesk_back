package com.flowdesk.repository;

import com.flowdesk.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOrganization_Id(Long organizationId);

    Optional<Task> findByIdAndOrganization_Id(
        Long id,
        Long organizationId
    );

    List<Task> findAllByProject_IdAndOrganization_Id(
        Long projectId,
        Long organizationId
    );
    List<Task> findAllByAssignedTo_IdAndOrganization_Id(
            Long userId,
            Long organizationId
    );
    Optional<Task> findByIdAndAssignedTo_IdAndOrganization_Id(
            Long taskId,
            Long userId,
            Long organizationId
    );
}