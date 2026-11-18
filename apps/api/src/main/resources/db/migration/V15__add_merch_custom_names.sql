ALTER TABLE order_items
    ADD COLUMN custom_name VARCHAR(20);

ALTER TABLE supplier_order_batch_items
    ADD COLUMN custom_name VARCHAR(20);

ALTER TABLE order_items
    DROP CONSTRAINT uk_order_items_order_product_size;

CREATE UNIQUE INDEX ux_order_items_order_product_size_custom_name
    ON order_items (order_id, product_code, size, COALESCE(custom_name, ''));
