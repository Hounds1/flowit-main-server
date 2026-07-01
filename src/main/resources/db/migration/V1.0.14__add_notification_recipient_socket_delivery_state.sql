ALTER TABLE notification_recipients
    ADD COLUMN socket_sent_at BIGINT UNSIGNED NULL COMMENT 'Unix epoch seconds when the server attempted websocket delivery.' AFTER hidden_at,
    ADD KEY idx_notification_recipients_user_socket_delivery (user_id, socket_sent_at, created_at, id);

UPDATE notification_recipients
SET socket_sent_at = created_at
WHERE socket_sent_at IS NULL;
