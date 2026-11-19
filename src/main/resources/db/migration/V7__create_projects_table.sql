
CREATE TABLE projects (
                          id BIGSERIAL PRIMARY KEY,

                          organization_id BIGINT NOT NULL,
                          customer_id BIGINT,

                          name VARCHAR(150) NOT NULL,
                          description TEXT,

                          status VARCHAR(30) NOT NULL DEFAULT 'PLANNING',

                          start_date DATE,
                          due_date DATE,

                          budget NUMERIC(12, 2),

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_projects_organization
                              FOREIGN KEY (organization_id)
                                  REFERENCES organizations(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_projects_customer
                              FOREIGN KEY (customer_id)
                                  REFERENCES customers(id)
                                  ON DELETE SET NULL,

                          CONSTRAINT chk_projects_status
                              CHECK (status IN (
                                                'PLANNING',
                                                'IN_PROGRESS',
                                                'ON_HOLD',
                                                'COMPLETED',
                                                'CANCELLED'
                                  ))
);

CREATE INDEX idx_projects_organization_id
    ON projects(organization_id);

CREATE INDEX idx_projects_customer_id
    ON projects(customer_id);
