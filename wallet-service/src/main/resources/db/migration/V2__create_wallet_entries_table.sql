CREATE TABLE wallet.wallet_entries
(
    id         UUID                     NOT NULL PRIMARY KEY,
    wallet_id  UUID                     NOT NULL REFERENCES wallet.wallets (id),
    payment_id UUID                     NOT NULL,
    type       VARCHAR(20)              NOT NULL,
    amount     NUMERIC(19, 4)           NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_wallet_entries_wallet_id  ON wallet.wallet_entries (wallet_id);
CREATE INDEX idx_wallet_entries_payment_id ON wallet.wallet_entries (payment_id);
