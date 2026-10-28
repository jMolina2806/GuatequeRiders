CREATE TABLE merch_settings (
    id         BIGINT PRIMARY KEY,
    open       BOOLEAN   NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP NOT NULL,
    updated_by_user_id BIGINT,
    CONSTRAINT ck_merch_settings_singleton CHECK (id = 1),
    CONSTRAINT fk_merch_settings_updated_by FOREIGN KEY (updated_by_user_id)
        REFERENCES users (id) ON DELETE SET NULL
);

INSERT INTO merch_settings (id, open, updated_at)
VALUES (1, FALSE, CURRENT_TIMESTAMP);
