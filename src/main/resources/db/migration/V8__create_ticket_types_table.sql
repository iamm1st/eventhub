CREATE TABLE ticket_types (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    total_quantity INTEGER NOT NULL,
    available_quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ticket_types_event
        FOREIGN KEY (event_id)
            REFERENCES events(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_ticket_types_price
        CHECK (price >= 0),

    CONSTRAINT chk_ticket_types_total_quantity
        CHECK (total_quantity > 0),

    CONSTRAINT chk_ticket_types_available_quantity
        CHECK (available_quantity >= 0),

    CONSTRAINT chk_ticket_types_available_not_greater_than_total
        CHECK (available_quantity <= total_quantity),

    CONSTRAINT uk_ticket_types_event_name
        UNIQUE (event_id, name)
);

CREATE INDEX idx_ticket_types_event_id ON ticket_types(event_id);