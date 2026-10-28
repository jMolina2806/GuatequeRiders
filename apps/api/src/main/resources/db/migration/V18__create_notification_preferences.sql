CREATE TABLE user_notification_preferences (
    user_id                BIGINT PRIMARY KEY,
    merch_notifications    BOOLEAN NOT NULL DEFAULT FALSE,
    route_notifications    BOOLEAN NOT NULL DEFAULT FALSE,
    event_notifications    BOOLEAN NOT NULL DEFAULT FALSE,
    circuit_notifications  BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_preferences_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
