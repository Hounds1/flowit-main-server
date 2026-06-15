ALTER TABLE workspace_members
    DROP INDEX uk_workspace_members_active_owner_workspace;

ALTER TABLE workspace_members
    DROP COLUMN active_owner_workspace_id;
