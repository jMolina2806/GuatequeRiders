ALTER TABLE users
    ADD COLUMN phone VARCHAR(20);

CREATE UNIQUE INDEX uk_users_phone
    ON users(phone)
    WHERE phone IS NOT NULL;
