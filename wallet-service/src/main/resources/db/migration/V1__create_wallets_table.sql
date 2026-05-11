CREATE TABLE wallet.wallets
(
    id               UUID                     NOT NULL PRIMARY KEY,
    user_id          UUID                     NOT NULL UNIQUE,
    balance          NUMERIC(19, 4)           NOT NULL,
    reserved_balance NUMERIC(19, 4)           NOT NULL DEFAULT 0,
    currency         VARCHAR(3)               NOT NULL,
    version          BIGINT                   NOT NULL DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_balance_non_negative          CHECK (balance >= 0),
    CONSTRAINT chk_reserved_balance_non_negative CHECK (reserved_balance >= 0),
    CONSTRAINT chk_reserved_lte_balance          CHECK (reserved_balance <= balance)
);

CREATE INDEX idx_wallets_user_id ON wallet.wallets (user_id);
