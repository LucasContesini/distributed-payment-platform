CREATE TABLE auth.refresh_tokens
(
    id          UUID                     NOT NULL PRIMARY KEY,
    user_id     UUID                     NOT NULL REFERENCES auth.users (id),
    token_hash  VARCHAR(255)             NOT NULL UNIQUE,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked     BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_refresh_tokens_token_hash ON auth.refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_user_id    ON auth.refresh_tokens (user_id);
