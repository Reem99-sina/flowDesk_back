package com.flowdesk.scheduler;

import com.flowdesk.entity.Task;
import com.flowdesk.entity.User;
import com.flowdesk.repository.TaskRepository;
import com.flowdesk.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeadlineReminderScheduler {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Africa/Cairo")
    @Transactional
    public void sendDeadlineReminders() {

        LocalDate reminderDate = LocalDate.now(
                ZoneId.of("Africa/Cairo")
        ).plusDays(1);

        List<Task> tasks = taskRepository
                .findTasksForDeadlineReminder(reminderDate);

        for (Task task : tasks) {

            User assignedUser = task.getAssignedTo();

            if (assignedUser == null) {
                continue;
            }

            if (!Boolean.TRUE.equals(
                    assignedUser.getDeadlineReminderNotifications()
            )) {
                continue;
            }

            notificationService.createDeadlineReminderNotification(
                    assignedUser,
                    task.getTitle(),
                    task.getDueDate()
            );

            task.setDeadlineReminderSent(true);
            task.setDeadlineReminderSentFor(task.getDueDate());
        }

        taskRepository.saveAll(tasks);
    }
}