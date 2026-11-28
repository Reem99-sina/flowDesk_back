package com.flowdesk.repository;

import com.flowdesk.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.flowdesk.enums.TaskStatus;
import java.time.LocalDate;
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


@Query("""
    SELECT t FROM Task t
    WHERE t.organization.id = :organizationId
    AND (
        :assignedToId IS NULL
        OR t.assignedTo.id = :assignedToId
    )
    AND (
        :search = ''
        OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(t.project.name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(t.assignedTo.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    AND (
        :status IS NULL
        OR t.status = :status
    )
    """)
Page<Task> searchTasks(
        @Param("organizationId") Long organizationId,
        @Param("assignedToId") Long assignedToId,
        @Param("search") String search,
        @Param("status") TaskStatus status,
        Pageable pageable
);
@Query("""
    SELECT
        COUNT(t),
        SUM(CASE WHEN t.status = com.flowdesk.enums.TaskStatus.DONE
            THEN 1 ELSE 0 END),
        SUM(CASE WHEN t.status IN (
            com.flowdesk.enums.TaskStatus.TODO,
            com.flowdesk.enums.TaskStatus.IN_PROGRESS,
            com.flowdesk.enums.TaskStatus.IN_REVIEW
        ) THEN 1 ELSE 0 END),
        SUM(CASE
            WHEN t.dueDate = :today
            AND t.status NOT IN (
                com.flowdesk.enums.TaskStatus.DONE,
                com.flowdesk.enums.TaskStatus.CANCELLED
            )
            THEN 1 ELSE 0
        END),
        SUM(CASE
            WHEN t.dueDate < :today
            AND t.status NOT IN (
                com.flowdesk.enums.TaskStatus.DONE,
                com.flowdesk.enums.TaskStatus.CANCELLED
            )
            THEN 1 ELSE 0
        END)
    FROM Task t
    WHERE t.organization.id = :organizationId
    AND (:assignedToId IS NULL OR t.assignedTo.id = :assignedToId)
    """)
List<Object[]> getTaskStatistics(
        @Param("organizationId") Long organizationId,
        @Param("assignedToId") Long assignedToId,
        @Param("today") java.time.LocalDate today
);

@Query("""
    SELECT t FROM Task t
    WHERE t.dueDate = :reminderDate
    AND t.assignedTo IS NOT NULL
    AND t.status NOT IN (
        com.flowdesk.enums.TaskStatus.DONE,
        com.flowdesk.enums.TaskStatus.CANCELLED
    )
    AND (
        t.deadlineReminderSent = false
        OR t.deadlineReminderSentFor IS NULL
        OR t.deadlineReminderSentFor <> :reminderDate
    )
    """)
List<Task> findTasksForDeadlineReminder(
        @Param("reminderDate") LocalDate reminderDate
);


}