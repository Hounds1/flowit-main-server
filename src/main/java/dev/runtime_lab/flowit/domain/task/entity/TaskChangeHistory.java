package dev.runtime_lab.flowit.domain.task.entity;

import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.workspace.entity.Workspace;
import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMember;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
	name = "task_change_histories",
	indexes = {
		@Index(name = "idx_task_change_histories_workspace_changed_at", columnList = "workspace_id,changed_at"),
		@Index(name = "idx_task_change_histories_task_changed_at", columnList = "task_id,changed_at"),
		@Index(name = "idx_task_change_histories_actor_member", columnList = "actor_workspace_member_id"),
		@Index(name = "idx_task_change_histories_actor_user", columnList = "actor_user_id"),
		@Index(name = "idx_task_change_histories_action", columnList = "action")
	}
)
public class TaskChangeHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "workspace_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_task_change_histories_workspace")
	)
	private Workspace workspace;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "task_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_task_change_histories_task")
	)
	private Task task;

	@Column(name = "task_title_snapshot", nullable = false, length = 100)
	private String taskTitleSnapshot;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "actor_workspace_member_id",
		foreignKey = @ForeignKey(name = "fk_task_change_histories_actor_member")
	)
	private WorkspaceMember actorWorkspaceMember;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "actor_user_id",
		foreignKey = @ForeignKey(name = "fk_task_change_histories_actor_user")
	)
	private User actorUser;

	@Column(name = "actor_display_name_snapshot", nullable = false, length = 100)
	private String actorDisplayNameSnapshot;

	@Enumerated(EnumType.STRING)
	@Column(name = "action", nullable = false, length = 30)
	private TaskHistoryAction action;

	@Column(name = "changes_json", nullable = false, columnDefinition = "LONGTEXT")
	private String changesJson;

	@Column(name = "changed_at", nullable = false)
	private Long changedAt;
}
