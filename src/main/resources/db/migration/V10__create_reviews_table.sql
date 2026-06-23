CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT fk_reviews_event
        FOREIGN KEY (event_id)
            REFERENCES events(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_reviews_rating
        CHECK (rating >= 1 AND rating <= 5),

    CONSTRAINT uk_reviews_user_event
        UNIQUE (user_id, event_id)
);

CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_event_id ON reviews(event_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);