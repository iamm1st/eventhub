CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    capacity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    rating NUMERIC(3, 2) NOT NULL DEFAULT 0,
    organizer_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_events_organizer
        FOREIGN KEY (organizer_id)
            REFERENCES users(id),

    CONSTRAINT fk_events_category
        FOREIGN KEY (category_id)
            REFERENCES event_categories(id),

    CONSTRAINT fk_events_location
        FOREIGN KEY (location_id)
            REFERENCES locations(id),

    CONSTRAINT chk_events_capacity
        CHECK (capacity > 0),

    CONSTRAINT chk_events_rating
        CHECK (rating >= 0 AND rating <= 5),

    CONSTRAINT chk_events_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'FINISHED')),

    CONSTRAINT chk_events_dates
        CHECK (end_date > start_date)
);

CREATE INDEX idx_events_organizer_id ON events(organizer_id);
CREATE INDEX idx_events_category_id ON events(category_id);
CREATE INDEX idx_events_location_id ON events(location_id);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_start_date ON events(start_date);
CREATE INDEX idx_events_rating ON events(rating);