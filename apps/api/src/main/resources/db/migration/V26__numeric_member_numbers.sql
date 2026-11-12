DO $$
DECLARE
    account RECORD;
    candidate BIGINT;
    numeric_member_number TEXT;
BEGIN
    FOR account IN
        SELECT id
        FROM users
        WHERE member_number IS NOT NULL
          AND member_number !~ '^GR-[0-9]+$'
        ORDER BY id
    LOOP
        candidate := account.id;

        LOOP
            numeric_member_number := 'GR-' || LPAD(candidate::TEXT, 4, '0');

            EXIT WHEN NOT EXISTS (
                SELECT 1
                FROM users
                WHERE member_number = numeric_member_number
            );

            candidate := candidate + 1;
        END LOOP;

        UPDATE users
        SET member_number = numeric_member_number,
            membership_version = membership_version + 1
        WHERE id = account.id;
    END LOOP;
END
$$;
