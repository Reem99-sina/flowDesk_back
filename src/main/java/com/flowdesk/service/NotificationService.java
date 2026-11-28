
package com.flowdesk.service;

import com.flowdesk.dto.notification.NotificationResponse;
import com.flowdesk.dto.notification.UnreadCountResponse;
import com.flowdesk.entity.Notification;
import com.flowdesk.entity.User;
import com.flowdesk.repository.NotificationRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {

        return notificationRepository
                .findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId) {

        long count = notificationRepository
                .countByUser_IdAndIsReadFalse(userId);

        return new UnreadCountResponse(count);
    }

    @Transactional
    public void createTaskAssignmentNotification(
            User assignedUser,
            String taskTitle
    ) {

        if (!Boolean.TRUE.equals(
                assignedUser.getTaskAssignmentNotifications()
        )) {
            return;
        }

        Notification notification = new Notification();

        notification.setUser(assignedUser);
        notification.setTitle("New Task Assigned");
        notification.setMessage(
                "You have been assigned a new task: " + taskTitle
        );
        notification.setType("TASK_ASSIGNED");
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }

    @Transactional
    public void createTaskUpdateNotification(
            User assignedUser,
            User updatedBy,
            String taskTitle
    ) {

        if (assignedUser == null || updatedBy == null) {
            return;
        }

        // Do not notify the user who made the update
        if (assignedUser.getId().equals(updatedBy.getId())) {
            return;
        }

        // Check the recipient's notification preferences
        if (!Boolean.TRUE.equals(
                assignedUser.getTaskUpdateNotifications()
        )) {
            return;
        }

        Notification notification = new Notification();

        notification.setUser(assignedUser);
        notification.setTitle("Task Updated");
        notification.setMessage(
                updatedBy.getFullName()
                        + " updated the task: "
                        + taskTitle
        );
        notification.setType("TASK_UPDATED");
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {

        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {

        Notification notification = notificationRepository
                .findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Notification not found")
                );

        notification.setIsRead(true);

        notificationRepository.save(notification);
    }

@Transactional
public void createDeadlineReminderNotification(
        User assignedUser,
        String taskTitle,
        LocalDate dueDate
) {

    if (assignedUser == null) {
        return;
    }

    if (!Boolean.TRUE.equals(
            assignedUser.getDeadlineReminderNotifications()
    )) {
        return;
    }

    Notification notification = new Notification();

    notification.setUser(assignedUser);
    notification.setTitle("Task Deadline Reminder");
    notification.setMessage(
            "Your task \"" + taskTitle
                    + "\" is due tomorrow (" + dueDate + ")."
    );
    notification.setType("TASK_DEADLINE_REMINDER");
    notification.setIsRead(false);

    notificationRepository.save(notification);
}

}
