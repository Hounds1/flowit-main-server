package dev.runtime_lab.flowit.domain.activity.entity;

import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordAction;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityTargetType;
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
import jakarta.persistence.UniqueConstraint;
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
	name = "workspace_activity_records",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_workspace_activity_records_source",
			columnNames = {"source_type", "source_id"}
		)
	},
	indexes = {
		@Index(name = "idx_workspace_activity_records_workspace_occurred", columnList = "workspace_id, occurred_at"),
		@Index(name = "idx_workspace_activity_records_workspace_domain", columnList = "workspace_id, domain"),
		@Index(name = "idx_workspace_activity_records_actor_member", columnList = "actor_workspace_member_id"),
		@Index(name = "idx_workspace_activity_records_actor_user", columnList = "actor_user_id"),
		@Index(name = "idx_workspace_activity_records_target", columnList = "target_type, target_id"),
		@Index(name = "idx_workspace_activity_records_action", columnList = "action")
	}
)
public class WorkspaceActivityRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "workspace_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_workspace_activity_records_workspace")
	)
	private Workspace workspace;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", nullable = false, length = 60)
	private ActivityRecordSourceType sourceType;

	@Column(name = "source_id", nullable = false)
	private Long sourceId;

	@Enumerated(EnumType.STRING)
	@Column(name = "domain", nullable = false, length = 40)
	private ActivityRecordDomain domain;

	@Enumerated(EnumType.STRING)
	@Column(name = "action", nullable = false, length = 40)
	private ActivityRecordAction action;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "actor_workspace_member_id",
		foreignKey = @ForeignKey(name = "fk_workspace_activity_records_actor_member")
	)
	private WorkspaceMember actorWorkspaceMember;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "actor_user_id",
		foreignKey = @ForeignKey(name = "fk_workspace_activity_records_actor_user")
	)
	private User actorUser;

	@Column(name = "actor_display_name_snapshot", nullable = false, length = 100)
	private String actorDisplayNameSnapshot;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_type", nullable = false, length = 40)
	private ActivityTargetType targetType;

	@Column(name = "target_id", nullable = false)
	private Long targetId;

	@Column(name = "target_display_name_snapshot", nullable = false, length = 100)
	private String targetDisplayNameSnapshot;

	@Column(name = "changes_json", nullable = false, columnDefinition = "LONGTEXT")
	private String changesJson;

	@Column(name = "occurred_at", nullable = false)
	private Long occurredAt;
}
