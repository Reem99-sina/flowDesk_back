package com.flowdesk.service;

import com.flowdesk.dto.request.CreateTaskRequest;
import com.flowdesk.dto.request.UpdateTaskRequest;
import com.flowdesk.dto.response.TaskResponse;
import com.flowdesk.entity.Organization;
import com.flowdesk.entity.Project;
import com.flowdesk.entity.Task;
import com.flowdesk.entity.User;
import com.flowdesk.enums.TaskPriority;
import com.flowdesk.enums.TaskStatus;
import com.flowdesk.enums.UserStatus;
import com.flowdesk.repository.OrganizationRepository;
import com.flowdesk.repository.ProjectRepository;
import com.flowdesk.repository.TaskRepository;
import com.flowdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assignedTo(
                        task.getAssignedTo() != null
                                ? task.getAssignedTo().getId()
                                : null
                )
                .assignedToName(
                        task.getAssignedTo() != null
                                ? task.getAssignedTo().getFullName()
                                : null
                )
                .status(task.getStatus())
                .priority(task.getPriority())
                .startDate(task.getStartDate())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private Project getProject(Long projectId, Long organizationId) {
        return projectRepository
                .findByIdAndOrganization_Id(projectId, organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found")
                );
    }

    private User getAssignedUser(Long userId, Long organizationId) {
        User user = userRepository
                .findByIdAndOrganization_Id(userId, organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Cannot assign task to an inactive user"
            );
        }

        return user;
    }

    private void validateDates(LocalDate startDate, LocalDate dueDate) {
        if (startDate != null && dueDate != null
                && dueDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "Due date cannot be before start date"
            );
        }
    }

    @Transactional
    public TaskResponse createTask(
            Long organizationId,
            CreateTaskRequest request
    ) {
        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Organization not found")
                );

        Project project = getProject(
                request.getProjectId(),
                organizationId
        );

        User assignedUser = request.getAssignedTo() != null
                ? getAssignedUser(request.getAssignedTo(), organizationId)
                : null;

        validateDates(request.getStartDate(), request.getDueDate());

        Task task = new Task();

        task.setOrganization(organization);
        task.setProject(project);
        task.setAssignedTo(assignedUser);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        task.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : TaskStatus.TODO
        );

        task.setPriority(
                request.getPriority() != null
                        ? request.getPriority()
                        : TaskPriority.MEDIUM
        );

        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());

        Task savedTask = taskRepository.save(task);

        return mapToResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks(Long organizationId) {
        return taskRepository
                .findAllByOrganization_Id(organizationId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(
            Long taskId,
            Long organizationId
    ) {
        Task task = taskRepository
                .findByIdAndOrganization_Id(taskId, organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Task not found")
                );

        return mapToResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(
            Long projectId,
            Long organizationId
    ) {
        getProject(projectId, organizationId);

        return taskRepository
                .findAllByProject_IdAndOrganization_Id(
                        projectId,
                        organizationId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

@Transactional
public TaskResponse updateTask(
        Long taskId,
        Long organizationId,
        UpdateTaskRequest request
) {
    Task task = taskRepository
            .findByIdAndOrganization_Id(taskId, organizationId)
            .orElseThrow(() ->
                    new IllegalArgumentException("Task not found")
            );

    if (request.getTitle() != null) {
        if (request.getTitle().isBlank()) {
            throw new IllegalArgumentException(
                    "Task title cannot be blank"
            );
        }
        task.setTitle(request.getTitle());
    }

    if (request.getDescription() != null) {
        task.setDescription(request.getDescription());
    }

    if (request.getProjectId() != null) {
        Project project = getProject(
                request.getProjectId(),
                organizationId
        );
        task.setProject(project);
    }

    if (request.getAssignedTo() != null) {
        User user = getAssignedUser(
                request.getAssignedTo(),
                organizationId
        );
        task.setAssignedTo(user);
    }

    if (request.getStatus() != null) {
        task.setStatus(request.getStatus());
    }

    if (request.getPriority() != null) {
        task.setPriority(request.getPriority());
    }

    if (request.getStartDate() != null) {
        task.setStartDate(request.getStartDate());
    }

    if (request.getDueDate() != null) {
        task.setDueDate(request.getDueDate());
    }

    validateDates(task.getStartDate(), task.getDueDate());

    Task updatedTask = taskRepository.save(task);

    return mapToResponse(updatedTask);
}
@Transactional
public void deleteTask(Long taskId, Long organizationId) {

    Task task = taskRepository
            .findByIdAndOrganization_Id(taskId, organizationId)
            .orElseThrow(() ->
                    new IllegalArgumentException("Task not found")
            );

    taskRepository.delete(task);
}
@Transactional(readOnly = true)
public List<TaskResponse> getMyTasks(
        Long userId,
        Long organizationId
) {
    return taskRepository
            .findAllByAssignedTo_IdAndOrganization_Id(
                    userId,
                    organizationId
            )
            .stream()
            .map(this::mapToResponse)
            .toList();
}

@Transactional
public TaskResponse updateMyTaskStatus(
        Long taskId,
        Long userId,
        Long organizationId,
        TaskStatus status
) {
    Task task = taskRepository
            .findByIdAndAssignedTo_IdAndOrganization_Id(
                    taskId,
                    userId,
                    organizationId
            )
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Task not found or not assigned to you"
                    )
            );

    task.setStatus(status);

    Task updatedTask = taskRepository.save(task);

    return mapToResponse(updatedTask);
}

}
