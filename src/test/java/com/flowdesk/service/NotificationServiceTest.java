package com.flowdesk.service;

import com.flowdesk.entity.Notification;
import com.flowdesk.entity.User;
import com.flowdesk.repository.NotificationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import java.util.Optional;

import static org.mockito.Mockito.when;
import com.flowdesk.dto.notification.UnreadCountResponse;
import com.flowdesk.dto.notification.NotificationResponse;
import java.util.List;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User assignedUser;

    @BeforeEach
    void setUp() {
        assignedUser = new User();
        assignedUser.setId(1L);
        assignedUser.setTaskAssignmentNotifications(true);
    }

    @Test
    void createTaskAssignmentNotification_shouldSaveNotification() {

        notificationService.createTaskAssignmentNotification(
                assignedUser,
                "Test Task"
        );

        verify(notificationRepository, times(1)).save(
                argThat(notification ->
                        notification.getUser().equals(assignedUser)
                                && notification.getTitle().equals("New Task Assigned")
                                && notification.getMessage().equals(
                                        "You have been assigned a new task: Test Task"
                                )
                                && notification.getType().equals("TASK_ASSIGNED")
                                && Boolean.FALSE.equals(notification.getIsRead())
                )
        );
    }

    @Test
    void createTaskAssignmentNotification_shouldNotSaveWhenPreferenceDisabled() {

        assignedUser.setTaskAssignmentNotifications(false);

        notificationService.createTaskAssignmentNotification(
                assignedUser,
                "Test Task"
        );

        verify(notificationRepository, never()).save(any(Notification.class));
    }
    @Test
    void createTaskUpdateNotification_shouldNotSaveWhenUserUpdatesOwnTask() {

        User updatedBy = new User();
        updatedBy.setId(1L);

        notificationService.createTaskUpdateNotification(
                assignedUser,
                updatedBy,
                "Test Task"
        );

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void createTaskUpdateNotification_shouldSaveWhenAnotherUserUpdatesTask() {

        User updatedBy = new User();
        updatedBy.setId(2L);
        updatedBy.setFullName("Test Admin");

        assignedUser.setTaskUpdateNotifications(true);

        notificationService.createTaskUpdateNotification(
                assignedUser,
                updatedBy,
                "Test Task"
        );

        verify(notificationRepository).save(
                argThat(notification ->
                        notification.getTitle().equals("Task Updated")
                                && notification.getMessage().equals(
                                        "Test Admin updated the task: Test Task"
                                )
                                && notification.getType().equals("TASK_UPDATED")
                )
        );
    }
    @Test
    void createDeadlineReminderNotification_shouldSaveWhenPreferenceEnabled() {

        assignedUser.setDeadlineReminderNotifications(true);

        notificationService.createDeadlineReminderNotification(
                assignedUser,
                "Test Task",
                java.time.LocalDate.of(2026, 11, 29)
        );

        verify(notificationRepository).save(
                argThat(notification ->
                        notification.getTitle().equals("Task Deadline Reminder")
                                && notification.getType().equals("TASK_DEADLINE_REMINDER")
                                && notification.getMessage().contains("Test Task")
                                && Boolean.FALSE.equals(notification.getIsRead())
                )
        );
    }

    @Test
    void createDeadlineReminderNotification_shouldNotSaveWhenPreferenceDisabled() {

        assignedUser.setDeadlineReminderNotifications(false);

        notificationService.createDeadlineReminderNotification(
                assignedUser,
                "Test Task",
                java.time.LocalDate.of(2026, 11, 29)
        );

        verify(notificationRepository, never()).save(any(Notification.class));
    }
    @Test
    void markAsRead_shouldMarkNotificationAsRead() {

        Notification notification = new Notification();
        notification.setId(10L);
        notification.setUser(assignedUser);
        notification.setIsRead(false);

        when(notificationRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead(10L, 1L);

        assertTrue(notification.getIsRead());

        verify(notificationRepository).save(notification);
    }
    @Test
    void getUnreadCount_shouldReturnCorrectCount() {

        when(notificationRepository.countByUser_IdAndIsReadFalse(1L))
                .thenReturn(3L);

        UnreadCountResponse response =
                notificationService.getUnreadCount(1L);

        assertEquals(3L, response.getUnreadCount());

        verify(notificationRepository)
                .countByUser_IdAndIsReadFalse(1L);
    }
    @Test
    void getNotifications_shouldReturnMappedNotifications() {

        Notification notification = new Notification();
        notification.setId(10L);
        notification.setUser(assignedUser);
        notification.setTitle("New Task Assigned");
        notification.setMessage("You have been assigned a new task: Test Task");
        notification.setType("TASK_ASSIGNED");
        notification.setIsRead(false);

        when(notificationRepository.findAllByUser_IdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponse> response =
                notificationService.getNotifications(1L);

        assertEquals(1, response.size());
        assertEquals("New Task Assigned", response.get(0).getTitle());
        assertEquals("TASK_ASSIGNED", response.get(0).getType());
        assertFalse(response.get(0).getIsRead());
    }
}