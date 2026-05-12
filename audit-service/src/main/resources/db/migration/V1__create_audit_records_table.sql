CREATE TABLE audit.audit_records
(
    id             UUID                     NOT NULL PRIMARY KEY,
    aggregate_id   UUID                     NOT NULL,
    event_type     VARCHAR(60)              NOT NULL,
    payload        TEXT                     NOT NULL,
    source_service VARCHAR(60)              NOT NULL,
    occurred_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    recorded_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_audit_records_aggregate_id  ON audit.audit_records (aggregate_id);
CREATE INDEX idx_audit_records_event_type    ON audit.audit_records (event_type);
CREATE INDEX idx_audit_records_occurred_at   ON audit.audit_records (occurred_at DESC);
