
ALTER TABLE tasks
    ADD COLUMN deadline_reminder_sent BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN deadline_reminder_sent_for DATE;
