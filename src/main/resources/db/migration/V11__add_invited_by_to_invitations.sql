ALTER TABLE invitations
    ADD COLUMN invited_by BIGINT;

ALTER TABLE invitations
    ADD CONSTRAINT fk_invitations_invited_by
        FOREIGN KEY (invited_by)
            REFERENCES users(id);