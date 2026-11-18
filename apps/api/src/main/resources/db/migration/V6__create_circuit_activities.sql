CREATE TABLE circuit_activities (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255)  NOT NULL,
    circuit_name     VARCHAR(255)  NOT NULL,
    description      VARCHAR(2000) NOT NULL,
    location         VARCHAR(255)  NOT NULL,
    circuit_date     DATE          NOT NULL,
    start_time       TIME          NOT NULL,
    end_time         TIME,
    price            NUMERIC(10, 2),
    discount         VARCHAR(255),
    benefit          VARCHAR(2000),
    registration_url VARCHAR(1000) NOT NULL,
    information_url  VARCHAR(1000),
    image_url        VARCHAR(1000),
    status           VARCHAR(20)   NOT NULL,
    created_at       TIMESTAMP     NOT NULL,
    CONSTRAINT ck_circuit_activities_price_non_negative
        CHECK (price IS NULL OR price >= 0)
);

CREATE INDEX idx_circuit_activities_status_date
    ON circuit_activities (status, circuit_date);
