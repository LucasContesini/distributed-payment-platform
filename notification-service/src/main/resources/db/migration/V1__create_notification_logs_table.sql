CREATE TABLE notification.notification_logs
(
    id           UUID                     NOT NULL PRIMARY KEY,
    recipient_id UUID,
    payment_id   UUID                     NOT NULL,
    type         VARCHAR(50)              NOT NULL,
    channel      VARCHAR(20)              NOT NULL,
    sent_at      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_notification_logs_recipient_id ON notification.notification_logs (recipient_id);
CREATE INDEX idx_notification_logs_payment_id   ON notification.notification_logs (payment_id);
