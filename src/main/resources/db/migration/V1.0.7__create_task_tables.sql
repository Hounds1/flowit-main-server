CREATE TABLE tasks (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '작업 식별자입니다.',
    workspace_id BIGINT UNSIGNED NOT NULL COMMENT '작업이 속한 워크스페이스 ID입니다. workspaces.id를 참조합니다.',
    title VARCHAR(100) NOT NULL COMMENT '작업 제목입니다.',
    description_markdown TEXT NULL COMMENT '작업 설명 Markdown 원문입니다.',
    status VARCHAR(30) NOT NULL COMMENT '작업 상태입니다. TO_DO, IN_PROGRESS, DONE 중 하나입니다.',
    priority VARCHAR(30) NOT NULL COMMENT '작업 우선순위입니다. HIGH, MEDIUM, LOW 중 하나입니다.',
    assignee_workspace_member_id BIGINT UNSIGNED NULL COMMENT '작업 담당 워크스페이스 멤버 ID입니다. 미할당 작업에서는 NULL이며, workspace_members.id를 참조합니다.',
    start_date DATE NULL COMMENT '작업 시작 예정일입니다.',
    due_date DATE NULL COMMENT '작업 마감 예정일입니다.',
    progress INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '작업 진행도입니다. 0부터 100까지의 정수입니다.',
    created_by_user_id BIGINT UNSIGNED NOT NULL COMMENT '작업 생성 사용자 ID입니다. users.id를 참조합니다.',
    created_at BIGINT UNSIGNED NOT NULL COMMENT '작업 생성 일시입니다. Unix epoch seconds 기준입니다.',
    updated_at BIGINT UNSIGNED NOT NULL COMMENT '작업 최종 수정 일시입니다. Unix epoch seconds 기준입니다.',
    deleted_at BIGINT UNSIGNED NULL COMMENT '작업 소프트 삭제 일시입니다. Unix epoch seconds 기준입니다.',

    PRIMARY KEY (id),
    KEY idx_tasks_workspace_status (workspace_id, status),
    KEY idx_tasks_workspace_assignee (workspace_id, assignee_workspace_member_id),
    KEY idx_tasks_workspace_due_date (workspace_id, due_date),
    KEY idx_tasks_workspace_updated_at (workspace_id, updated_at),
    KEY idx_tasks_created_by_user_id (created_by_user_id),
    KEY idx_tasks_deleted_at (deleted_at),

    CONSTRAINT chk_tasks_progress_range
        CHECK (progress BETWEEN 0 AND 100),

    CONSTRAINT fk_tasks_workspace
        FOREIGN KEY (workspace_id)
            REFERENCES workspaces (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_tasks_assignee_workspace_member
        FOREIGN KEY (assignee_workspace_member_id)
            REFERENCES workspace_members (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_tasks_created_by_user
        FOREIGN KEY (created_by_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='워크스페이스 작업 테이블입니다.';

CREATE TABLE task_tags (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '작업 태그 식별자입니다.',
    task_id BIGINT UNSIGNED NOT NULL COMMENT '태그가 붙은 작업 ID입니다. tasks.id를 참조합니다.',
    name VARCHAR(30) NOT NULL COMMENT '태그 표시 이름입니다.',
    normalized_name VARCHAR(30) NOT NULL COMMENT '중복 검사용 정규화 태그 이름입니다.',
    sort_order INT UNSIGNED NOT NULL COMMENT '작업 내 태그 표시 순서입니다.',
    created_at BIGINT UNSIGNED NOT NULL COMMENT '태그 생성 일시입니다. Unix epoch seconds 기준입니다.',

    PRIMARY KEY (id),
    UNIQUE KEY uk_task_tags_task_normalized_name (task_id, normalized_name),
    KEY idx_task_tags_normalized_name (normalized_name),

    CONSTRAINT fk_task_tags_task
        FOREIGN KEY (task_id)
            REFERENCES tasks (id)
            ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='작업 태그 테이블입니다.';

CREATE TABLE task_change_histories (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '작업 변경 이력 식별자입니다.',
    workspace_id BIGINT UNSIGNED NOT NULL COMMENT '변경이 발생한 워크스페이스 ID입니다. workspaces.id를 참조합니다.',
    task_id BIGINT UNSIGNED NOT NULL COMMENT '변경 대상 작업 ID입니다. tasks.id를 참조합니다.',
    task_title_snapshot VARCHAR(100) NOT NULL COMMENT '변경 발생 당시 작업 제목 스냅샷입니다.',
    actor_workspace_member_id BIGINT UNSIGNED NULL COMMENT '변경을 수행한 워크스페이스 멤버 ID입니다. 시스템 작업에서는 NULL일 수 있습니다.',
    actor_user_id BIGINT UNSIGNED NULL COMMENT '변경을 수행한 사용자 ID입니다. 시스템 작업에서는 NULL일 수 있습니다.',
    actor_display_name_snapshot VARCHAR(100) NOT NULL COMMENT '변경 발생 당시 행위자 표시 이름 스냅샷입니다.',
    action VARCHAR(30) NOT NULL COMMENT '고수준 변경 의미입니다.',
    changes_json LONGTEXT NOT NULL COMMENT '세부 변경 의미 목록입니다. JSON 배열 문자열입니다.',
    changed_at BIGINT UNSIGNED NOT NULL COMMENT '변경 발생 일시입니다. Unix epoch seconds 기준입니다.',

    PRIMARY KEY (id),
    KEY idx_task_change_histories_workspace_changed_at (workspace_id, changed_at),
    KEY idx_task_change_histories_task_changed_at (task_id, changed_at),
    KEY idx_task_change_histories_actor_member (actor_workspace_member_id),
    KEY idx_task_change_histories_actor_user (actor_user_id),
    KEY idx_task_change_histories_action (action),

    CONSTRAINT fk_task_change_histories_workspace
        FOREIGN KEY (workspace_id)
            REFERENCES workspaces (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_task_change_histories_task
        FOREIGN KEY (task_id)
            REFERENCES tasks (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_task_change_histories_actor_member
        FOREIGN KEY (actor_workspace_member_id)
            REFERENCES workspace_members (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_task_change_histories_actor_user
        FOREIGN KEY (actor_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='작업 변경 이력 테이블입니다.';
CREATE TABLE workspace_activity_records (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Workspace activity record id.',
    workspace_id BIGINT UNSIGNED NOT NULL COMMENT 'Workspace id where the activity occurred.',
    source_type VARCHAR(60) NOT NULL COMMENT 'Source history type for this activity record.',
    source_id BIGINT UNSIGNED NOT NULL COMMENT 'Source history id for this activity record.',
    domain VARCHAR(40) NOT NULL COMMENT 'Activity domain.',
    action VARCHAR(40) NOT NULL COMMENT 'Semantic activity action.',
    actor_workspace_member_id BIGINT UNSIGNED NULL COMMENT 'Workspace member id of the actor.',
    actor_user_id BIGINT UNSIGNED NULL COMMENT 'User id of the actor.',
    actor_display_name_snapshot VARCHAR(100) NOT NULL COMMENT 'Actor display name at activity time.',
    target_type VARCHAR(40) NOT NULL COMMENT 'Activity target type.',
    target_id BIGINT UNSIGNED NOT NULL COMMENT 'Activity target id.',
    target_display_name_snapshot VARCHAR(100) NOT NULL COMMENT 'Target display name at activity time.',
    changes_json LONGTEXT NOT NULL COMMENT 'Semantic activity changes as a JSON array string.',
    occurred_at BIGINT UNSIGNED NOT NULL COMMENT 'Activity occurrence time as Unix epoch seconds.',

    PRIMARY KEY (id),
    UNIQUE KEY uk_workspace_activity_records_source (source_type, source_id),
    KEY idx_workspace_activity_records_workspace_occurred (workspace_id, occurred_at),
    KEY idx_workspace_activity_records_workspace_domain (workspace_id, domain),
    KEY idx_workspace_activity_records_actor_member (actor_workspace_member_id),
    KEY idx_workspace_activity_records_actor_user (actor_user_id),
    KEY idx_workspace_activity_records_target (target_type, target_id),
    KEY idx_workspace_activity_records_action (action),

    CONSTRAINT fk_workspace_activity_records_workspace
        FOREIGN KEY (workspace_id)
            REFERENCES workspaces (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_workspace_activity_records_actor_member
        FOREIGN KEY (actor_workspace_member_id)
            REFERENCES workspace_members (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_workspace_activity_records_actor_user
        FOREIGN KEY (actor_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Workspace-level activity feed records.';
