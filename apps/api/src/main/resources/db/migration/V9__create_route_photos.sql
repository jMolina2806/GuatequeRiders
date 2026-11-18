CREATE TABLE route_photos (
    id              BIGSERIAL PRIMARY KEY,
    route_id        BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    original_name   VARCHAR(255),
    stored_filename VARCHAR(80)  NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    size_bytes      BIGINT       NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    CONSTRAINT fk_route_photos_route FOREIGN KEY (route_id)
        REFERENCES routes (id) ON DELETE CASCADE,
    CONSTRAINT fk_route_photos_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT uk_route_photos_route_stored_filename UNIQUE (route_id, stored_filename),
    CONSTRAINT ck_route_photos_size CHECK (size_bytes > 0 AND size_bytes <= 5242880)
);

CREATE INDEX idx_route_photos_route_created
    ON route_photos (route_id, created_at, id);

CREATE INDEX idx_route_photos_route_user
    ON route_photos (route_id, user_id);
