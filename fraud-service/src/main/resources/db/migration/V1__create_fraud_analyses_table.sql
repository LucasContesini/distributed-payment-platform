CREATE TABLE fraud.fraud_analyses
(
    id          UUID                     NOT NULL PRIMARY KEY,
    payment_id  UUID                     NOT NULL UNIQUE,
    payer_id    UUID                     NOT NULL,
    amount      NUMERIC(19, 4)           NOT NULL,
    decision    VARCHAR(20)              NOT NULL,
    reason      VARCHAR(255)             NOT NULL,
    analyzed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_fraud_analyses_payment_id ON fraud.fraud_analyses (payment_id);
CREATE INDEX idx_fraud_analyses_payer_id   ON fraud.fraud_analyses (payer_id);
