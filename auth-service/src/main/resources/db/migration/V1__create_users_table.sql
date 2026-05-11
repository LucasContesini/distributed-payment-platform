CREATE TABLE auth.users
(
    id            UUID                     NOT NULL PRIMARY KEY,
    email         VARCHAR(255)             NOT NULL UNIQUE,
    password_hash VARCHAR(255)             NOT NULL,
    role          VARCHAR(50)              NOT NULL,
    status        VARCHAR(50)              NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_users_email ON auth.users (email);
