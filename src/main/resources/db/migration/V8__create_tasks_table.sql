CREATE TABLE tasks (
                       id BIGSERIAL PRIMARY KEY,

                       organization_id BIGINT NOT NULL,
                       project_id BIGINT NOT NULL,
                       assigned_to BIGINT,

                       title VARCHAR(150) NOT NULL,
                       description TEXT,

                       status VARCHAR(30) NOT NULL DEFAULT 'TODO',
                       priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',

                       start_date DATE,
                       due_date DATE,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_tasks_organization
                           FOREIGN KEY (organization_id)
                               REFERENCES organizations(id) ON DELETE CASCADE,

                       CONSTRAINT fk_tasks_project
                           FOREIGN KEY (project_id)
                               REFERENCES projects(id) ON DELETE CASCADE,

                       CONSTRAINT fk_tasks_assigned_user
                           FOREIGN KEY (assigned_to)
                               REFERENCES users(id) ON DELETE SET NULL,

                       CONSTRAINT chk_tasks_status
                           CHECK (status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'CANCELLED')),

                       CONSTRAINT chk_tasks_priority
                           CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
);

CREATE INDEX idx_tasks_organization_id ON tasks(organization_id);
CREATE INDEX idx_tasks_project_id ON tasks(project_id);
CREATE INDEX idx_tasks_assigned_to ON tasks(assigned_to);