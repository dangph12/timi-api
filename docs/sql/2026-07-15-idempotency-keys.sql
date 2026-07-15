-- Idempotency keys + payment transaction hardening
-- Run manually against Postgres BEFORE deploying app (ddl-auto: validate).

-- Pre-check: existing duplicate PENDING rows block partial index creation. Clean first if rows returned.
-- SELECT order_id FROM payment_transaction WHERE status = 'PENDING' GROUP BY order_id HAVING count(*) > 1;

ALTER TABLE "order" ADD COLUMN idempotency_key VARCHAR(64);
CREATE UNIQUE INDEX uq_order_idempotency_key ON "order" (idempotency_key);

ALTER TABLE payment_transaction ADD COLUMN idempotency_key VARCHAR(64);
CREATE UNIQUE INDEX uq_payment_tx_idempotency_key ON payment_transaction (idempotency_key);

-- backstop verify: ensure reference unique index actually exists
CREATE UNIQUE INDEX IF NOT EXISTS uq_payment_tx_reference ON payment_transaction (transaction_reference);

-- one PENDING transaction per order, DB-level backstop for COD race
CREATE UNIQUE INDEX uq_payment_tx_pending_per_order
    ON payment_transaction (order_id) WHERE status = 'PENDING';
