CREATE TABLE notification_alerts (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '알림 식별자.',
    source_type VARCHAR(60) NOT NULL COMMENT '알림을 생성한 원천 유형.',
    source_id BIGINT UNSIGNED NOT NULL COMMENT '알림을 생성한 원천 식별자.',
    type VARCHAR(80) NOT NULL COMMENT '알림 의미 유형.',
    scope_type VARCHAR(40) NOT NULL COMMENT '알림 범위 유형.',
    scope_id BIGINT UNSIGNED NOT NULL COMMENT '알림 범위 식별자.',
    scope_name_snapshot VARCHAR(100) NOT NULL COMMENT '알림 생성 시점의 범위 표시 이름.',
    actor_type VARCHAR(40) NULL COMMENT '알림 발생 주체 유형.',
    actor_id BIGINT UNSIGNED NULL COMMENT '알림 발생 주체 식별자.',
    actor_name_snapshot VARCHAR(100) NULL COMMENT '알림 생성 시점의 발생 주체 표시 이름.',
    actor_profile_image_url VARCHAR(500) NULL COMMENT '알림 생성 시점의 발생 주체 프로필 이미지 URL.',
    subject_type VARCHAR(40) NOT NULL COMMENT '알림 대상 유형.',
    subject_id BIGINT UNSIGNED NOT NULL COMMENT '알림 대상 식별자.',
    subject_name_snapshot VARCHAR(100) NOT NULL COMMENT '알림 생성 시점의 대상 표시 이름.',
    changes_json LONGTEXT NOT NULL COMMENT '알림 변경 요소 JSON 배열 문자열.',
    link_type VARCHAR(40) NOT NULL COMMENT '클라이언트 이동 링크 유형.',
    link_workspace_id BIGINT UNSIGNED NULL COMMENT '클라이언트 이동에 사용할 워크스페이스 식별자.',
    occurred_at BIGINT UNSIGNED NOT NULL COMMENT '도메인 이벤트 발생 시각. 유닉스 에포크 초 단위.',
    created_at BIGINT UNSIGNED NOT NULL COMMENT '알림 생성 시각. 유닉스 에포크 초 단위.',

    PRIMARY KEY (id),
    UNIQUE KEY uk_notification_alerts_source (source_type, source_id, type),
    KEY idx_notification_alerts_scope_created (scope_type, scope_id, created_at, id),
    KEY idx_notification_alerts_type (type),
    KEY idx_notification_alerts_occurred (occurred_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='저장된 알림 기록.';

CREATE TABLE notification_recipients (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '알림 수신자 식별자.',
    notification_alert_id BIGINT UNSIGNED NOT NULL COMMENT '알림 식별자.',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '수신 사용자 식별자.',
    created_at BIGINT UNSIGNED NOT NULL COMMENT '수신자별 알림 생성 시각. 유닉스 에포크 초 단위.',
    seen_at BIGINT UNSIGNED NULL COMMENT '수신자별 알림 확인 시각. 유닉스 에포크 초 단위.',
    read_at BIGINT UNSIGNED NULL COMMENT '수신자별 알림 읽음 시각. 유닉스 에포크 초 단위.',
    hidden_at BIGINT UNSIGNED NULL COMMENT '수신자별 알림 숨김 시각. 유닉스 에포크 초 단위.',

    PRIMARY KEY (id),
    UNIQUE KEY uk_notification_recipients_alert_user (notification_alert_id, user_id),
    KEY idx_notification_recipients_user_created (user_id, created_at, id),
    KEY idx_notification_recipients_user_seen (user_id, seen_at),
    KEY idx_notification_recipients_user_read (user_id, read_at),
    KEY idx_notification_recipients_alert (notification_alert_id),

    CONSTRAINT fk_notification_recipients_alert
        FOREIGN KEY (notification_alert_id)
            REFERENCES notification_alerts (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_notification_recipients_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='사용자별 알림 전달 및 읽음 상태 기록.';

CREATE TABLE outbox_events (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '아웃박스 이벤트 식별자.',
    event_type VARCHAR(80) NOT NULL COMMENT '아웃박스 이벤트 유형.',
    payload_json LONGTEXT NOT NULL COMMENT '아웃박스 이벤트 페이로드 JSON 문자열.',
    status VARCHAR(30) NOT NULL COMMENT '아웃박스 처리 상태.',
    created_at BIGINT UNSIGNED NOT NULL COMMENT '아웃박스 이벤트 생성 시각. 유닉스 에포크 초 단위.',
    updated_at BIGINT UNSIGNED NOT NULL COMMENT '아웃박스 이벤트 마지막 수정 시각. 유닉스 에포크 초 단위.',
    processed_at BIGINT UNSIGNED NULL COMMENT '아웃박스 이벤트 처리 완료 시각. 유닉스 에포크 초 단위.',
    failed_at BIGINT UNSIGNED NULL COMMENT '아웃박스 이벤트 처리 실패 시각. 유닉스 에포크 초 단위.',
    failure_message VARCHAR(500) NULL COMMENT '마지막 아웃박스 이벤트 실패 메시지.',

    PRIMARY KEY (id),
    KEY idx_outbox_events_status_created (status, created_at, id),
    KEY idx_outbox_events_type (event_type)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='트랜잭션 아웃박스 이벤트.';
