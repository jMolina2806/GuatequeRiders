CREATE TABLE supplier_order_batches (
    id           BIGSERIAL PRIMARY KEY,
    campaign_id  BIGINT         NOT NULL UNIQUE,
    closed_at    TIMESTAMP      NOT NULL,
    total_units  INTEGER        NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    created_at   TIMESTAMP      NOT NULL,
    CONSTRAINT fk_supplier_order_batch_campaign FOREIGN KEY (campaign_id)
        REFERENCES merch_campaigns (id) ON DELETE RESTRICT,
    CONSTRAINT ck_supplier_order_batch_total_units CHECK (total_units >= 0),
    CONSTRAINT ck_supplier_order_batch_total_amount CHECK (total_amount >= 0)
);

CREATE TABLE supplier_order_batch_orders (
    id                BIGSERIAL PRIMARY KEY,
    batch_id          BIGINT         NOT NULL,
    original_order_id BIGINT         NOT NULL,
    customer_name     VARCHAR(255)   NOT NULL,
    order_created_at  TIMESTAMP      NOT NULL,
    total_amount      NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_supplier_batch_order_batch FOREIGN KEY (batch_id)
        REFERENCES supplier_order_batches (id) ON DELETE CASCADE,
    CONSTRAINT fk_supplier_batch_order_original FOREIGN KEY (original_order_id)
        REFERENCES orders (id) ON DELETE RESTRICT,
    CONSTRAINT uk_supplier_batch_order_original UNIQUE (batch_id, original_order_id),
    CONSTRAINT ck_supplier_batch_order_total CHECK (total_amount >= 0)
);

CREATE TABLE supplier_order_batch_items (
    id             BIGSERIAL PRIMARY KEY,
    batch_order_id BIGINT         NOT NULL,
    product_code   VARCHAR(20)    NOT NULL,
    product_name   VARCHAR(255)   NOT NULL,
    size           VARCHAR(10)    NOT NULL,
    quantity       INTEGER        NOT NULL,
    unit_price     NUMERIC(10, 2) NOT NULL,
    subtotal       NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_supplier_batch_item_order FOREIGN KEY (batch_order_id)
        REFERENCES supplier_order_batch_orders (id) ON DELETE CASCADE,
    CONSTRAINT ck_supplier_batch_item_product CHECK (product_code IN ('TSHIRT', 'CROP_TOP', 'HOODIE')),
    CONSTRAINT ck_supplier_batch_item_size CHECK (size IN ('S', 'M', 'L', 'XL')),
    CONSTRAINT ck_supplier_batch_item_quantity CHECK (quantity > 0 AND quantity <= 10),
    CONSTRAINT ck_supplier_batch_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT ck_supplier_batch_item_subtotal CHECK (subtotal >= 0)
);

CREATE INDEX idx_supplier_batch_orders_batch ON supplier_order_batch_orders (batch_id, id);
CREATE INDEX idx_supplier_batch_items_order ON supplier_order_batch_items (batch_order_id, id);

INSERT INTO supplier_order_batches (campaign_id, closed_at, total_units, total_amount, created_at)
SELECT campaign.id,
       COALESCE(campaign.closed_at, campaign.updated_at),
       COALESCE(units.total_units, 0),
       COALESCE(amounts.total_amount, 0),
       COALESCE(campaign.closed_at, campaign.updated_at)
FROM merch_campaigns campaign
LEFT JOIN (
    SELECT order_row.campaign_id, CAST(SUM(item.quantity) AS INTEGER) AS total_units
    FROM orders order_row
    JOIN order_items item ON item.order_id = order_row.id
    WHERE order_row.payment_status = 'PAID'
    GROUP BY order_row.campaign_id
) units ON units.campaign_id = campaign.id
LEFT JOIN (
    SELECT campaign_id, SUM(total_amount) AS total_amount
    FROM orders
    WHERE payment_status = 'PAID'
    GROUP BY campaign_id
) amounts ON amounts.campaign_id = campaign.id
WHERE campaign.status IN ('CLOSED', 'ORDERED');

INSERT INTO supplier_order_batch_orders (
    batch_id,
    original_order_id,
    customer_name,
    order_created_at,
    total_amount
)
SELECT batch.id,
       order_row.id,
       order_row.customer_name,
       order_row.created_at,
       order_row.total_amount
FROM supplier_order_batches batch
JOIN orders order_row ON order_row.campaign_id = batch.campaign_id
WHERE order_row.payment_status = 'PAID';

INSERT INTO supplier_order_batch_items (
    batch_order_id,
    product_code,
    product_name,
    size,
    quantity,
    unit_price,
    subtotal
)
SELECT batch_order.id,
       item.product_code,
       item.product_name,
       item.size,
       item.quantity,
       item.unit_price,
       item.subtotal
FROM supplier_order_batch_orders batch_order
JOIN order_items item ON item.order_id = batch_order.original_order_id;
