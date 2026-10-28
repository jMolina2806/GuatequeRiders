CREATE TABLE collaborators (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    type                VARCHAR(20)  NOT NULL,
    short_description   VARCHAR(500) NOT NULL,
    discount_percentage SMALLINT,
    benefit             VARCHAR(1000),
    website_url         VARCHAR(1000),
    image_url           VARCHAR(500),
    featured            BOOLEAN      NOT NULL DEFAULT FALSE,
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    CONSTRAINT ck_collaborators_type CHECK (
        type IN ('WORKSHOP', 'SHOP', 'BRAND', 'EQUIPMENT', 'PHOTOGRAPHY', 'CIRCUIT', 'OTHER')
    ),
    CONSTRAINT ck_collaborators_discount_percentage CHECK (
        discount_percentage IS NULL OR discount_percentage BETWEEN 0 AND 100
    )
);

CREATE INDEX idx_collaborators_active_featured
    ON collaborators (active, featured);

CREATE INDEX idx_collaborators_type
    ON collaborators (type);
