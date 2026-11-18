ALTER TABLE users
    ADD COLUMN member_number VARCHAR(32),
    ADD COLUMN membership_active BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN membership_valid_until DATE,
    ADD COLUMN membership_version BIGINT NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX uk_users_member_number
    ON users(member_number)
    WHERE member_number IS NOT NULL;
