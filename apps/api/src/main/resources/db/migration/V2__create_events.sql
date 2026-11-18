CREATE TABLE events (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255)  NOT NULL,
    description      VARCHAR(2000) NOT NULL,
    location         VARCHAR(255)  NOT NULL,
    event_date       DATE          NOT NULL,
    start_time       TIME          NOT NULL,
    end_time         TIME,
    max_participants INTEGER,
    status           VARCHAR(20)   NOT NULL,
    created_at       TIMESTAMP     NOT NULL
);

CREATE TABLE event_registrations (
    id            BIGSERIAL PRIMARY KEY,
    event_id      BIGINT    NOT NULL,
    user_id       BIGINT    NOT NULL,
    registered_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_event_registrations_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_registrations_user  FOREIGN KEY (user_id)  REFERENCES users  (id) ON DELETE CASCADE,
    CONSTRAINT uk_event_registrations_event_user UNIQUE (event_id, user_id)
);

CREATE INDEX idx_event_registrations_event ON event_registrations (event_id);
CREATE INDEX idx_event_registrations_user  ON event_registrations (user_id);
