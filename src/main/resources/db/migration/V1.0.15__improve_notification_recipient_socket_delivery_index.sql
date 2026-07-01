ALTER TABLE notification_recipients
    DROP INDEX idx_notification_recipients_user_socket_delivery,
    ADD KEY idx_notification_recipients_user_socket_delivery (user_id, socket_sent_at, hidden_at, created_at, id);
