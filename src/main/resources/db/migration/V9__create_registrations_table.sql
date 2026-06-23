CREATE TABLE registrations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    ticket_type_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_registrations_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT fk_registrations_event
        FOREIGN KEY (event_id)
            REFERENCES events(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_registrations_ticket_type
        FOREIGN KEY (ticket_type_id)
            REFERENCES ticket_types(id),

    CONSTRAINT chk_registrations_status
        CHECK (status IN ('ACTIVE', 'CANCELLED'))
);

CREATE INDEX idx_registrations_user_id ON registrations(user_id);
CREATE INDEX idx_registrations_event_id ON registrations(event_id);
CREATE INDEX idx_registrations_ticket_type_id ON registrations(ticket_type_id);
CREATE INDEX idx_registrations_status ON registrations(status);

CREATE UNIQUE INDEX uk_registrations_active_user_event
    ON registrations(user_id, event_id)
    WHERE status = 'ACTIVE';