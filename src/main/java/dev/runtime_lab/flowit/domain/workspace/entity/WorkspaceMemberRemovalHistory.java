package dev.runtime_lab.flowit.domain.workspace.entity;

import dev.runtime_lab.flowit.domain.user.entity.User;
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
	name = "workspace_member_removal_histories",
	indexes = {
		@Index(name = "idx_workspace_member_removal_histories_workspace_id", columnList = "workspace_id"),
		@Index(name = "idx_workspace_member_removal_histories_member_id", columnList = "workspace_member_id"),
		@Index(name = "idx_workspace_member_removal_histories_target_user_id", columnList = "target_user_id"),
		@Index(name = "idx_workspace_member_removal_histories_removed_at", columnList = "removed_at")
	}
)
public class WorkspaceMemberRemovalHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "workspace_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_workspace_member_removal_histories_workspace")
	)
	private Workspace workspace;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "workspace_member_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_workspace_member_removal_histories_member")
	)
	private WorkspaceMember workspaceMember;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "target_user_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_workspace_member_removal_histories_target_user")
	)
	private User targetUser;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_snapshot", nullable = false, length = 30)
	private WorkspaceMemberRole roleSnapshot;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "removed_by_user_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_workspace_member_removal_histories_removed_by_user")
	)
	private User removedBy;

	@Column(name = "removed_at", nullable = false)
	private Long removedAt;
}
