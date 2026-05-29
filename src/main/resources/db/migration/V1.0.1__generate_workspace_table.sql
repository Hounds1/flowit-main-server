CREATE TABLE workspaces (
                            id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '워크스페이스 식별자.',
                            name VARCHAR(100) NOT NULL COMMENT '워크스페이스 이름.',
                            description VARCHAR(500) NULL COMMENT '워크스페이스 설명. 선택 입력.',
                            invite_code CHAR(14) NOT NULL COMMENT '워크스페이스 초대 코드. 예: A1B2-C3D4-E5F6',
                            created_by_user_id BIGINT UNSIGNED NOT NULL COMMENT '워크스페이스를 최초 생성한 사용자 ID. users.id를 참조',
                            created_at BIGINT UNSIGNED NOT NULL COMMENT '워크스페이스 생성 일시. Unix epoch seconds 기준.',
                            updated_at BIGINT UNSIGNED NOT NULL COMMENT '워크스페이스 최종 수정 일시. Unix epoch seconds 기준.',
                            deleted_at BIGINT UNSIGNED NULL COMMENT '워크스페이스 소프트 삭제 일시. Unix epoch seconds 기준.',

                            PRIMARY KEY (id),
                            UNIQUE KEY uk_workspaces_invite_code (invite_code),
                            KEY idx_workspaces_created_by_user_id (created_by_user_id),
                            KEY idx_workspaces_deleted_at (deleted_at),

                            CONSTRAINT fk_workspaces_created_by_user
                                FOREIGN KEY (created_by_user_id)
                                    REFERENCES users (id)
                                    ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='워크스페이스 테이블. 이름, 설명, 초대 코드와 생성자를 관리.';

CREATE TABLE workspace_members (
                                   id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '워크스페이스 구성원 식별자.',
                                   workspace_id BIGINT UNSIGNED NOT NULL COMMENT '워크스페이스 ID. workspaces.id를 참조',
                                   user_id BIGINT UNSIGNED NOT NULL COMMENT '구성원 사용자 ID. users.id를 참조',
                                   role VARCHAR(30) NOT NULL DEFAULT 'MEMBER' COMMENT '워크스페이스 권한. LEADER, MEMBER 등은 애플리케이션 enum으로 관리',
                                   joined_at BIGINT UNSIGNED NOT NULL COMMENT '워크스페이스 참여 일시. Unix epoch seconds 기준.',
                                   created_at BIGINT UNSIGNED NOT NULL COMMENT '구성원 레코드 생성 일시. Unix epoch seconds 기준.',
                                   updated_at BIGINT UNSIGNED NOT NULL COMMENT '구성원 레코드 최종 수정 일시. Unix epoch seconds 기준.',
                                   deleted_at BIGINT UNSIGNED NULL COMMENT '구성원 소프트 삭제 일시. Unix epoch seconds 기준.',

                                   active_workspace_user_key VARCHAR(255)
                                       GENERATED ALWAYS AS (
                                           CASE
                                               WHEN deleted_at IS NULL THEN CONCAT(workspace_id, ':', user_id)
                                               ELSE NULL
                                               END
                                           ) STORED COMMENT '활성 구성원 중복 가입 방지를 위한 생성 컬럼.',

                                   active_leader_workspace_id BIGINT UNSIGNED
                                       GENERATED ALWAYS AS (
                                           CASE
                                               WHEN deleted_at IS NULL AND role = 'LEADER' THEN workspace_id
                                               ELSE NULL
                                               END
                                           ) STORED COMMENT '활성 워크스페이스 리더 단일 보장을 위한 생성 컬럼.',

                                   PRIMARY KEY (id),
                                   UNIQUE KEY uk_workspace_members_active_workspace_user (active_workspace_user_key),
                                   UNIQUE KEY uk_workspace_members_active_leader_workspace (active_leader_workspace_id),
                                   KEY idx_workspace_members_workspace_id (workspace_id),
                                   KEY idx_workspace_members_user_id (user_id),
                                   KEY idx_workspace_members_role (role),
                                   KEY idx_workspace_members_deleted_at (deleted_at),

                                   CONSTRAINT fk_workspace_members_workspace
                                       FOREIGN KEY (workspace_id)
                                           REFERENCES workspaces (id)
                                           ON DELETE RESTRICT,

                                   CONSTRAINT fk_workspace_members_user
                                       FOREIGN KEY (user_id)
                                           REFERENCES users (id)
                                           ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='워크스페이스 구성원 테이블. 사용자별 워크스페이스 참여와 권한을 관리.';