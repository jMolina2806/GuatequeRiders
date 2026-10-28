CREATE TABLE user_notification_preferences (
    user_id                BIGINT PRIMARY KEY,
    merch_notifications    BOOLEAN NOT NULL DEFAULT TRUE,
    route_notifications    BOOLEAN NOT NULL DEFAULT TRUE,
    event_notifications    BOOLEAN NOT NULL DEFAULT TRUE,
    circuit_notifications  BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_preferences_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
