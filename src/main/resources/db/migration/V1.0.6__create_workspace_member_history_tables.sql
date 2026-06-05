CREATE TABLE workspace_member_role_histories (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '워크스페이스 멤버 역할 변경 이력 식별자입니다.',
    workspace_id BIGINT UNSIGNED NOT NULL COMMENT '역할 변경이 발생한 워크스페이스 ID입니다. workspaces.id를 참조합니다.',
    workspace_member_id BIGINT UNSIGNED NOT NULL COMMENT '역할이 변경된 워크스페이스 멤버 ID입니다. workspace_members.id를 참조합니다.',
    from_role VARCHAR(30) NULL COMMENT '변경 전 역할입니다. 기준 이력에서는 NULL일 수 있습니다.',
    to_role VARCHAR(30) NOT NULL COMMENT '변경 후 역할입니다.',
    changed_by_user_id BIGINT UNSIGNED NULL COMMENT '역할 변경을 수행한 사용자 ID입니다. 시스템 보정 이력에서는 NULL일 수 있습니다.',
    changed_at BIGINT UNSIGNED NOT NULL COMMENT '역할 변경 발생 일시입니다. Unix epoch seconds 기준입니다.',

    PRIMARY KEY (id),
    KEY idx_workspace_member_role_histories_workspace_id (workspace_id),
    KEY idx_workspace_member_role_histories_member_id (workspace_member_id),
    KEY idx_workspace_member_role_histories_to_role (to_role),
    KEY idx_workspace_member_role_histories_changed_at (changed_at),

    CONSTRAINT fk_workspace_member_role_histories_workspace
        FOREIGN KEY (workspace_id)
            REFERENCES workspaces (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_workspace_member_role_histories_member
        FOREIGN KEY (workspace_member_id)
            REFERENCES workspace_members (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_workspace_member_role_histories_changed_by_user
        FOREIGN KEY (changed_by_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='워크스페이스 멤버 역할 변경 이력을 보관하는 테이블입니다.';

INSERT INTO workspace_member_role_histories (
    workspace_id,
    workspace_member_id,
    from_role,
    to_role,
    changed_by_user_id,
    changed_at
)
SELECT
    workspace_id,
    id,
    NULL,
    role,
    NULL,
    updated_at
FROM workspace_members
WHERE deleted_at IS NULL;

CREATE TABLE workspace_member_removal_histories (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '워크스페이스 멤버 강제 퇴장 이력 식별자입니다.',
    workspace_id BIGINT UNSIGNED NOT NULL COMMENT '강제 퇴장이 발생한 워크스페이스 ID입니다. workspaces.id를 참조합니다.',
    workspace_member_id BIGINT UNSIGNED NOT NULL COMMENT '강제 퇴장된 워크스페이스 멤버 ID입니다. workspace_members.id를 참조합니다.',
    target_user_id BIGINT UNSIGNED NOT NULL COMMENT '강제 퇴장된 사용자 ID입니다. users.id를 참조합니다.',
    role_snapshot VARCHAR(30) NOT NULL COMMENT '강제 퇴장 당시 대상 멤버의 역할입니다.',
    removed_by_user_id BIGINT UNSIGNED NOT NULL COMMENT '강제 퇴장을 수행한 사용자 ID입니다. users.id를 참조합니다.',
    removed_at BIGINT UNSIGNED NOT NULL COMMENT '강제 퇴장 발생 일시입니다. Unix epoch seconds 기준입니다.',

    PRIMARY KEY (id),
    KEY idx_workspace_member_removal_histories_workspace_id (workspace_id),
    KEY idx_workspace_member_removal_histories_member_id (workspace_member_id),
    KEY idx_workspace_member_removal_histories_target_user_id (target_user_id),
    KEY idx_workspace_member_removal_histories_removed_at (removed_at),

    CONSTRAINT fk_workspace_member_removal_histories_workspace
        FOREIGN KEY (workspace_id)
            REFERENCES workspaces (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_workspace_member_removal_histories_member
        FOREIGN KEY (workspace_member_id)
            REFERENCES workspace_members (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_workspace_member_removal_histories_target_user
        FOREIGN KEY (target_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_workspace_member_removal_histories_removed_by_user
        FOREIGN KEY (removed_by_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='워크스페이스 멤버 강제 퇴장 이력을 보관하는 테이블입니다.';

CREATE TABLE workspace_member_withdrawal_histories (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '워크스페이스 멤버 탈퇴 이력 식별자입니다.',
    workspace_id BIGINT UNSIGNED NOT NULL COMMENT '탈퇴가 발생한 워크스페이스 ID입니다. workspaces.id를 참조합니다.',
    workspace_member_id BIGINT UNSIGNED NOT NULL COMMENT '탈퇴한 워크스페이스 멤버 ID입니다. workspace_members.id를 참조합니다.',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '탈퇴한 사용자 ID입니다. users.id를 참조합니다.',
    role_snapshot VARCHAR(30) NOT NULL COMMENT '탈퇴 당시 멤버의 역할입니다.',
    ownership_transferred_to_workspace_member_id BIGINT UNSIGNED NULL COMMENT 'OWNER 탈퇴로 소유권을 이전받은 워크스페이스 멤버 ID입니다. 소유권 이전이 없으면 NULL입니다.',
    ownership_transferred_to_user_id BIGINT UNSIGNED NULL COMMENT 'OWNER 탈퇴로 소유권을 이전받은 사용자 ID입니다. 소유권 이전이 없으면 NULL입니다.',
    withdrawn_at BIGINT UNSIGNED NOT NULL COMMENT '탈퇴 발생 일시입니다. Unix epoch seconds 기준입니다.',

    PRIMARY KEY (id),
    KEY idx_workspace_member_withdrawal_histories_workspace_id (workspace_id),
    KEY idx_workspace_member_withdrawal_histories_member_id (workspace_member_id),
    KEY idx_workspace_member_withdrawal_histories_user_id (user_id),
    KEY idx_workspace_member_withdrawal_histories_withdrawn_at (withdrawn_at),

    CONSTRAINT fk_workspace_member_withdrawal_histories_workspace
        FOREIGN KEY (workspace_id)
            REFERENCES workspaces (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_workspace_member_withdrawal_histories_member
        FOREIGN KEY (workspace_member_id)
            REFERENCES workspace_members (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_workspace_member_withdrawal_histories_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_workspace_member_withdrawal_histories_transferred_member
        FOREIGN KEY (ownership_transferred_to_workspace_member_id)
            REFERENCES workspace_members (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_workspace_member_withdrawal_histories_transferred_user
        FOREIGN KEY (ownership_transferred_to_user_id)
            REFERENCES users (id)
            ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='워크스페이스 멤버 자진 탈퇴 이력을 보관하는 테이블입니다.';
