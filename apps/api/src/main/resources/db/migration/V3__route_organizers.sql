CREATE TABLE route_organizers (
    id       BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    user_id  BIGINT NOT NULL,
    CONSTRAINT fk_route_organizers_route FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE,
    CONSTRAINT fk_route_organizers_user  FOREIGN KEY (user_id)  REFERENCES users  (id) ON DELETE CASCADE,
    CONSTRAINT uk_route_organizers_route_user UNIQUE (route_id, user_id)
);

CREATE INDEX idx_route_organizers_route ON route_organizers (route_id);
CREATE INDEX idx_route_organizers_user  ON route_organizers (user_id);
