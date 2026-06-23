CREATE TABLE organizer_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    organization_name VARCHAR(150) NOT NULL,
    contact_email VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(30),
    description TEXT NOT NULL,
    website_url VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    admin_comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,

    CONSTRAINT fk_organizer_applications_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_organizer_applications_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_organizer_applications_user_id
    ON organizer_applications(user_id);

CREATE INDEX idx_organizer_applications_status
    ON organizer_applications(status);

CREATE UNIQUE INDEX uk_organizer_applications_active_user
    ON organizer_applications(user_id)
    WHERE status IN ('PENDING', 'APPROVED');