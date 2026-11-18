-- 1. Remove route organizers whose user is not an ADMIN.
DELETE FROM route_organizers
WHERE user_id IN (
    SELECT id FROM users WHERE role <> 'ADMIN'
);

-- 2. If any route has been left without organizers and no ADMIN exists to
--    assign, fail clearly instead of leaving inconsistent data.
DO $$
BEGIN
    IF (SELECT count(*) FROM users WHERE role = 'ADMIN') = 0
       AND EXISTS (
           SELECT 1 FROM routes r
           WHERE NOT EXISTS (
               SELECT 1 FROM route_organizers ro WHERE ro.route_id = r.id
           )
       ) THEN
        RAISE EXCEPTION 'Cannot migrate: routes without organizers and no ADMIN users exist';
    END IF;
END $$;

-- 3. Assign the ADMIN with the lowest id to routes left without organizers.
INSERT INTO route_organizers (route_id, user_id)
SELECT r.id,
       (SELECT id FROM users WHERE role = 'ADMIN' ORDER BY id ASC LIMIT 1)
FROM routes r
WHERE NOT EXISTS (
    SELECT 1 FROM route_organizers ro WHERE ro.route_id = r.id
);
