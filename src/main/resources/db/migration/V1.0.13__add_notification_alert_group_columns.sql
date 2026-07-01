ALTER TABLE notification_alerts
    ADD COLUMN group_id VARCHAR(120) NULL COMMENT '같은 원인 이벤트에서 파생된 알림 묶음 식별자. 내부 정렬용이며 응답에는 노출하지 않는다.' AFTER occurred_at,
    ADD COLUMN group_sequence INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '같은 알림 묶음 안의 내부 발송 순서. 응답에는 노출하지 않는다.' AFTER group_id,
    ADD KEY idx_notification_alerts_group (group_id, group_sequence, id);
