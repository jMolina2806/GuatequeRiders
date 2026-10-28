ALTER TABLE orders
    DROP CONSTRAINT ck_orders_status;

ALTER TABLE orders
    ADD CONSTRAINT ck_orders_status
        CHECK (status IN (
            'PENDING',
            'CONFIRMED',
            'IN_PRODUCTION',
            'READY',
            'DELIVERED',
            'CANCELLED'
        ));
