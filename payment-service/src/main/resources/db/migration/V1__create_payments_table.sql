CREATE TABLE payment.payments
(
    id               UUID                     NOT NULL PRIMARY KEY,
    payer_id         UUID                     NOT NULL,
    payee_id         UUID                     NOT NULL,
    amount           NUMERIC(19, 4)           NOT NULL,
    currency         VARCHAR(3)               NOT NULL,
    status           VARCHAR(50)              NOT NULL,
    idempotency_key  VARCHAR(64)              NOT NULL UNIQUE,
    rejection_reason VARCHAR(255),
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_payments_payer_id        ON payment.payments (payer_id);
CREATE INDEX idx_payments_idempotency_key ON payment.payments (idempotency_key);
CREATE INDEX idx_payments_status          ON payment.payments (status);
