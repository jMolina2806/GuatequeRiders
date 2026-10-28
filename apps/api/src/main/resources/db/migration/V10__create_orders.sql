CREATE TABLE orders (
    id                         BIGSERIAL PRIMARY KEY,
    user_id                    BIGINT         NOT NULL,
    status                     VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    payment_status             VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    total_amount               NUMERIC(10, 2) NOT NULL,
    customer_name              VARCHAR(255)   NOT NULL,
    customer_email             VARCHAR(255)   NOT NULL,
    customer_phone             VARCHAR(50)    NOT NULL,
    notes                      VARCHAR(1000),
    fulfillment_method         VARCHAR(20),
    address                    VARCHAR(1000),
    city                       VARCHAR(255),
    postal_code                VARCHAR(30),
    province                   VARCHAR(120),
    paid_at                    TIMESTAMP,
    created_at                 TIMESTAMP      NOT NULL,
    updated_at                 TIMESTAMP      NOT NULL,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_orders_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    CONSTRAINT ck_orders_payment_status CHECK (
        payment_status IN ('PENDING', 'PAID', 'FAILED', 'CANCELLED')
    ),
    CONSTRAINT ck_orders_fulfillment_method CHECK (
        fulfillment_method IS NULL OR fulfillment_method IN ('PICKUP', 'SHIPPING')
    ),
    CONSTRAINT ck_orders_total_non_negative CHECK (total_amount >= 0)
);

CREATE TABLE order_items (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    product_code VARCHAR(20)    NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    size         VARCHAR(10)    NOT NULL,
    quantity     INTEGER        NOT NULL,
    unit_price   NUMERIC(10, 2) NOT NULL,
    subtotal     NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT ck_order_items_product_code CHECK (product_code IN ('TSHIRT', 'CROP_TOP', 'HOODIE')),
    CONSTRAINT ck_order_items_size CHECK (size IN ('S', 'M', 'L', 'XL')),
    CONSTRAINT ck_order_items_quantity CHECK (quantity > 0 AND quantity <= 10),
    CONSTRAINT ck_order_items_unit_price_non_negative CHECK (unit_price >= 0),
    CONSTRAINT ck_order_items_subtotal_non_negative CHECK (subtotal >= 0),
    CONSTRAINT uk_order_items_order_product_size UNIQUE (order_id, product_code, size)
);

CREATE INDEX idx_orders_user_created ON orders (user_id, created_at, id);
CREATE INDEX idx_orders_payment_status ON orders (payment_status, created_at, id);
CREATE INDEX idx_order_items_order ON order_items (order_id);
