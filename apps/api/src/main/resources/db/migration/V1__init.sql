CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email    UNIQUE (email)
);

CREATE TABLE routes (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255)  NOT NULL,
    description      VARCHAR(2000) NOT NULL,
    origin           VARCHAR(255)  NOT NULL,
    destination      VARCHAR(255)  NOT NULL,
    route_date       DATE          NOT NULL,
    distance_km      DOUBLE PRECISION,
    difficulty       VARCHAR(20)   NOT NULL,
    max_participants INTEGER       NOT NULL,
    status           VARCHAR(20)   NOT NULL
);

CREATE TABLE route_registrations (
    id            BIGSERIAL PRIMARY KEY,
    route_id      BIGINT    NOT NULL,
    user_id       BIGINT    NOT NULL,
    registered_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_registrations_route FOREIGN KEY (route_id) REFERENCES routes (id) ON DELETE CASCADE,
    CONSTRAINT fk_registrations_user  FOREIGN KEY (user_id)  REFERENCES users  (id) ON DELETE CASCADE,
    CONSTRAINT uk_registrations_route_user UNIQUE (route_id, user_id)
);
