CREATE TABLE payment_attempts (
    id                           BIGSERIAL PRIMARY KEY,
    order_id                     BIGINT       NOT NULL,
    provider                     VARCHAR(20)  NOT NULL DEFAULT 'STRIPE',
    stripe_checkout_session_id   VARCHAR(255),
    stripe_payment_intent_id     VARCHAR(255),
    status                       VARCHAR(20)  NOT NULL,
    amount_minor                 BIGINT       NOT NULL,
    currency                     VARCHAR(3)   NOT NULL,
    idempotency_key              VARCHAR(255) NOT NULL UNIQUE,
    checkout_url                 VARCHAR(1000),
    expires_at                   TIMESTAMP,
    created_at                   TIMESTAMP    NOT NULL,
    updated_at                   TIMESTAMP    NOT NULL,
    completed_at                 TIMESTAMP,
    CONSTRAINT fk_payment_attempt_order FOREIGN KEY (order_id)
        REFERENCES orders (id) ON DELETE RESTRICT,
    CONSTRAINT ck_payment_attempt_provider CHECK (provider = 'STRIPE'),
    CONSTRAINT ck_payment_attempt_status CHECK (
        status IN ('CREATED', 'OPEN', 'SUCCEEDED', 'FAILED', 'EXPIRED', 'CANCELLED')
    ),
    CONSTRAINT ck_payment_attempt_amount CHECK (amount_minor > 0),
    CONSTRAINT ck_payment_attempt_currency CHECK (currency = UPPER(currency))
);

CREATE UNIQUE INDEX ux_payment_attempt_session
    ON payment_attempts (stripe_checkout_session_id)
    WHERE stripe_checkout_session_id IS NOT NULL;

CREATE UNIQUE INDEX ux_payment_attempt_payment_intent
    ON payment_attempts (stripe_payment_intent_id)
    WHERE stripe_payment_intent_id IS NOT NULL;

CREATE UNIQUE INDEX ux_payment_attempt_active_order
    ON payment_attempts (order_id)
    WHERE status IN ('CREATED', 'OPEN');

CREATE INDEX idx_payment_attempt_order_status
    ON payment_attempts (order_id, status, id);

CREATE TABLE stripe_events (
    id                   BIGSERIAL PRIMARY KEY,
    stripe_event_id     VARCHAR(255) NOT NULL UNIQUE,
    event_type          VARCHAR(120) NOT NULL,
    object_id           VARCHAR(255),
    livemode            BOOLEAN      NOT NULL,
    payment_attempt_id  BIGINT,
    status              VARCHAR(20)  NOT NULL,
    failure_reason      VARCHAR(500),
    received_at         TIMESTAMP    NOT NULL,
    processed_at        TIMESTAMP,
    CONSTRAINT fk_stripe_event_payment_attempt FOREIGN KEY (payment_attempt_id)
        REFERENCES payment_attempts (id) ON DELETE SET NULL,
    CONSTRAINT ck_stripe_event_status CHECK (
        status IN ('RECEIVED', 'PROCESSING', 'PROCESSED', 'FAILED')
    )
);

CREATE INDEX idx_stripe_events_attempt
    ON stripe_events (payment_attempt_id, received_at);
