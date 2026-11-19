CREATE TABLE customers (
                           id BIGSERIAL PRIMARY KEY,
                           organization_id BIGINT NOT NULL,
                           name VARCHAR(150) NOT NULL,
                           email VARCHAR(255),
                           phone VARCHAR(30),
                           company VARCHAR(150),
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_customers_organization
                               FOREIGN KEY (organization_id)
                                   REFERENCES organizations(id)
                                   ON DELETE CASCADE,

                           CONSTRAINT uk_customers_organization_email
                               UNIQUE (organization_id, email)
);