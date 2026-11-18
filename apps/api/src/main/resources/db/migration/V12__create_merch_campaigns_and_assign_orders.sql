CREATE TABLE merch_campaigns (
    id                  BIGSERIAL PRIMARY KEY,
    status              VARCHAR(20) NOT NULL,
    opened_at           TIMESTAMP   NOT NULL,
    closed_at           TIMESTAMP,
    ordered_at          TIMESTAMP,
    opened_by_user_id   BIGINT,
    closed_by_user_id   BIGINT,
    ordered_by_user_id  BIGINT,
    created_at          TIMESTAMP   NOT NULL,
    updated_at          TIMESTAMP   NOT NULL,
    CONSTRAINT ck_merch_campaign_status CHECK (status IN ('OPEN', 'CLOSED', 'ORDERED')),
    CONSTRAINT fk_merch_campaign_opened_by FOREIGN KEY (opened_by_user_id)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_merch_campaign_closed_by FOREIGN KEY (closed_by_user_id)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_merch_campaign_ordered_by FOREIGN KEY (ordered_by_user_id)
        REFERENCES users (id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX ux_merch_campaign_single_open
    ON merch_campaigns (status)
    WHERE status = 'OPEN';

INSERT INTO merch_campaigns (status, opened_at, closed_at, created_at, updated_at)
SELECT CASE WHEN open THEN 'OPEN' ELSE 'CLOSED' END,
       updated_at,
       CASE WHEN open THEN NULL ELSE updated_at END,
       updated_at,
       updated_at
FROM merch_settings
WHERE id = 1;

ALTER TABLE orders ADD COLUMN campaign_id BIGINT;
ALTER TABLE orders ADD COLUMN late_payment BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE orders
SET campaign_id = (SELECT id FROM merch_campaigns ORDER BY id LIMIT 1)
WHERE campaign_id IS NULL;

ALTER TABLE orders ALTER COLUMN campaign_id SET NOT NULL;
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_campaign FOREIGN KEY (campaign_id)
        REFERENCES merch_campaigns (id) ON DELETE RESTRICT;

CREATE INDEX idx_orders_campaign_created ON orders (campaign_id, created_at, id);
CREATE INDEX idx_orders_campaign_payment ON orders (campaign_id, payment_status, created_at, id);
