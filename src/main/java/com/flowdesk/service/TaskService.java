package com.flowdesk.service;
import java.util.Objects;
import com.flowdesk.dto.request.CreateTaskRequest;
import com.flowdesk.dto.request.UpdateTaskRequest;
import com.flowdesk.dto.response.TaskResponse;
import com.flowdesk.entity.Organization;
import com.flowdesk.entity.Project;
import com.flowdesk.entity.Task;
import com.flowdesk.entity.User;
import com.flowdesk.enums.TaskPriority;
import com.flowdesk.enums.UserStatus;
import com.flowdesk.repository.OrganizationRepository;
import com.flowdesk.repository.ProjectRepository;
import com.flowdesk.repository.TaskRepository;
import com.flowdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.flowdesk.enums.ActivityType;
import com.flowdesk.enums.TaskStatus;
import com.flowdesk.dto.response.DashboardSummaryResponse;


@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final NotificationService notificationService;

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
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();

    }

private void updateCompletionTimestamp(
        Task task,
        TaskStatus newStatus
) {
    if (newStatus == TaskStatus.DONE) {
        if (task.getCompletedAt() == null) {
            task.setCompletedAt(LocalDateTime.now());
        }
    } else {
        task.setCompletedAt(null);
    }
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
            Long userId,
            CreateTaskRequest request
    ) {
        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Organization not found")
                );
        User createdBy = userRepository
                .findByIdAndOrganization_Id(userId, organizationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
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

        if (task.getStatus() == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        }

        task.setPriority(
                request.getPriority() != null
                        ? request.getPriority()
                        : TaskPriority.MEDIUM
        );

        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());

        Task savedTask = taskRepository.save(task);

        if (assignedUser != null) {
            notificationService.createTaskAssignmentNotification(
                    assignedUser,
                    savedTask.getTitle()
            );
        }
        activityService.createActivity(
                createdBy,
                organization,
                ActivityType.TASK_CREATED,
                "created a new task",
                savedTask.getTitle(),
                "TASK",
                savedTask.getId()
        );

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
            Long organizationId,
            Long userId,
            boolean isAgent
    ) {
        Task task;

        if (isAgent) {
            task = taskRepository
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
        } else {
            task = taskRepository
                    .findByIdAndOrganization_Id(taskId, organizationId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Task not found")
                    );
        }

        return mapToResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(
            Long projectId,
            Long organizationId,
            Long userId,
            boolean isAgent
    ) {
        getProject(projectId, organizationId);

        List<Task> tasks;

        if (isAgent) {
            tasks = taskRepository
                    .findAllByProject_IdAndOrganization_Id(projectId, organizationId)
                    .stream()
                    .filter(task ->
                            task.getAssignedTo() != null &&
                            task.getAssignedTo().getId().equals(userId)
                    )
                    .toList();
        } else {
            tasks = taskRepository
                    .findAllByProject_IdAndOrganization_Id(
                            projectId,
                            organizationId
                    );
        }

        return tasks.stream()
                .map(this::mapToResponse)
                .toList();
    }


@Transactional
public TaskResponse updateTask(
        Long taskId,
        Long organizationId,
        Long userId,
        UpdateTaskRequest request
) {
    Task task = taskRepository
            .findByIdAndOrganization_Id(taskId, organizationId)
            .orElseThrow(() ->
                    new IllegalArgumentException("Task not found")
            );

    TaskStatus oldStatus = task.getStatus();

    Long previousAssignedUserId = task.getAssignedTo() != null
            ? task.getAssignedTo().getId()
            : null;

    User updatedBy = userRepository
            .findByIdAndOrganization_Id(userId, organizationId)
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found")
            );

    boolean taskChanged = false;
    boolean assignmentChanged = false;

    if (request.getTitle() != null) {
        if (request.getTitle().isBlank()) {
            throw new IllegalArgumentException(
                    "Task title cannot be blank"
            );
        }

        if (!Objects.equals(task.getTitle(), request.getTitle())) {
            task.setTitle(request.getTitle());
            taskChanged = true;
        }
    }

    if (request.getDescription() != null) {
        if (!Objects.equals(task.getDescription(), request.getDescription())) {
            task.setDescription(request.getDescription());
            taskChanged = true;
        }
    }

    if (request.getProjectId() != null) {
        if (!task.getProject().getId().equals(request.getProjectId())) {
            Project project = getProject(
                    request.getProjectId(),
                    organizationId
            );

            task.setProject(project);
            taskChanged = true;
        }
    }

    if (request.getAssignedTo() != null) {
        if (!request.getAssignedTo().equals(previousAssignedUserId)) {
            User user = getAssignedUser(
                    request.getAssignedTo(),
                    organizationId
            );

            task.setAssignedTo(user);
            taskChanged = true;
            assignmentChanged = true;
        }
    }

    if (request.getStatus() != null) {
        if (task.getStatus() != request.getStatus()) {
            updateCompletionTimestamp(task, request.getStatus());
            task.setStatus(request.getStatus());
            taskChanged = true;
        }
    }

    if (request.getPriority() != null) {
        if (task.getPriority() != request.getPriority()) {
            task.setPriority(request.getPriority());
            taskChanged = true;
        }
    }

    if (request.getStartDate() != null) {
        if (!Objects.equals(task.getStartDate(), request.getStartDate())) {
            task.setStartDate(request.getStartDate());
            taskChanged = true;
        }
    }

    if (request.getDueDate() != null &&
            !Objects.equals(task.getDueDate(), request.getDueDate())) {

        task.setDueDate(request.getDueDate());

        task.setDeadlineReminderSent(false);
        task.setDeadlineReminderSentFor(null);

        taskChanged = true;
    }

    validateDates(task.getStartDate(), task.getDueDate());

    Task updatedTask = taskRepository.save(task);

    User newAssignedUser = updatedTask.getAssignedTo();

    // Notify the new assignee when the task is reassigned
    if (newAssignedUser != null &&
            !newAssignedUser.getId().equals(previousAssignedUserId)) {

        notificationService.createTaskAssignmentNotification(
                newAssignedUser,
                updatedTask.getTitle()
        );
    }

    // Notify the current assignee about other task updates
    // Do not send a duplicate notification after reassignment
    if (taskChanged &&
            !assignmentChanged &&
            newAssignedUser != null) {

        notificationService.createTaskUpdateNotification(
                newAssignedUser,
                updatedBy,
                updatedTask.getTitle()
        );
    }

    if (request.getStatus() != null &&
            oldStatus != request.getStatus()) {

        ActivityType activityType =
                request.getStatus() == TaskStatus.DONE
                        ? ActivityType.TASK_COMPLETED
                        : ActivityType.TASK_STATUS_UPDATED;

        String description =
                request.getStatus() == TaskStatus.DONE
                        ? "completed a task"
                        : "updated task status";

        activityService.createActivity(
                updatedBy,
                updatedTask.getOrganization(),
                activityType,
                description,
                updatedTask.getTitle(),
                "TASK",
                updatedTask.getId()
        );
    }

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
    TaskStatus oldStatus = task.getStatus();

    User updatedBy = userRepository
            .findByIdAndOrganization_Id(userId, organizationId)
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found")
            );

    updateCompletionTimestamp(task, status);
    task.setStatus(status);

    Task updatedTask = taskRepository.save(task);
    if (oldStatus != status) {

        ActivityType activityType =
                status == TaskStatus.DONE
                        ? ActivityType.TASK_COMPLETED
                        : ActivityType.TASK_STATUS_UPDATED;

        String description =
                status == TaskStatus.DONE
                        ? "completed a task"
                        : "updated task status";

        activityService.createActivity(
                updatedBy,
                updatedTask.getOrganization(),
                activityType,
                description,
                updatedTask.getTitle(),
                "TASK",
                updatedTask.getId()
        );
    }

    return mapToResponse(updatedTask);
}

@Transactional(readOnly = true)
public Page<TaskResponse> searchTasks(
        Long organizationId,
        Long assignedToId,
        String search,
        TaskStatus status,
        Pageable pageable
) {
    String searchValue = (search == null || search.isBlank())
            ? ""
            : search.trim();

    return taskRepository
            .searchTasks(
                    organizationId,
                    assignedToId,
                    searchValue,
                    status,
                    pageable
            )
            .map(this::mapToResponse);
}
@Transactional(readOnly = true)
public DashboardSummaryResponse getTaskStatistics(
        Long organizationId,
        Long assignedToId
) {
    List<Object[]> rows = taskRepository.getTaskStatistics(
            organizationId,
            assignedToId,
            LocalDate.now()
    );

    Object[] result = rows.isEmpty()
            ? new Object[]{0L, 0L, 0L, 0L, 0L}
            : rows.get(0);

    return DashboardSummaryResponse.builder()
            .totalTasks(toLong(result[0]))
            .completedTasks(toLong(result[1]))
            .pendingTasks(toLong(result[2]))
            .todaysTasks(toLong(result[3]))
            .overdueTasks(toLong(result[4]))
            .build();
}

private Long toLong(Object value) {
    return value == null ? 0L : ((Number) value).longValue();
}
}
