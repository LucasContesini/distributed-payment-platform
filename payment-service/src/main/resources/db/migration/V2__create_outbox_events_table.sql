CREATE TABLE payment.outbox_events
(
    id           UUID                     NOT NULL PRIMARY KEY,
    aggregate_id UUID                     NOT NULL,
    event_type   VARCHAR(100)             NOT NULL,
    topic        VARCHAR(100)             NOT NULL,
    payload      TEXT                     NOT NULL,
    published    BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_unpublished ON payment.outbox_events (published, created_at)
    WHERE published = FALSE;
