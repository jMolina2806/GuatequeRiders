ALTER TABLE users
    ADD COLUMN privacy_acknowledged_at TIMESTAMP,
    ADD COLUMN privacy_policy_version VARCHAR(40);

ALTER TABLE orders
    ADD COLUMN merch_terms_accepted_at TIMESTAMP,
    ADD COLUMN merch_terms_version VARCHAR(40);
