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
	name = "workspace_member_role_histories",
	indexes = {
		@Index(name = "idx_workspace_member_role_histories_workspace_id", columnList = "workspace_id"),
		@Index(name = "idx_workspace_member_role_histories_member_id", columnList = "workspace_member_id"),
		@Index(name = "idx_workspace_member_role_histories_to_role", columnList = "to_role"),
		@Index(name = "idx_workspace_member_role_histories_changed_at", columnList = "changed_at")
	}
)
public class WorkspaceMemberRoleHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "workspace_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_workspace_member_role_histories_workspace")
	)
	private Workspace workspace;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "workspace_member_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_workspace_member_role_histories_member")
	)
	private WorkspaceMember workspaceMember;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_role", length = 30)
	private WorkspaceMemberRole fromRole;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_role", nullable = false, length = 30)
	private WorkspaceMemberRole toRole;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "changed_by_user_id",
		foreignKey = @ForeignKey(name = "fk_workspace_member_role_histories_changed_by_user")
	)
	private User changedBy;

	@Column(name = "changed_at", nullable = false)
	private Long changedAt;
}
