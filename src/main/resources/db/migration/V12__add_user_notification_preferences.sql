
ALTER TABLE users
    ADD COLUMN email_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN task_assignment_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN task_update_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN deadline_reminder_notifications BOOLEAN NOT NULL DEFAULT TRUE;
