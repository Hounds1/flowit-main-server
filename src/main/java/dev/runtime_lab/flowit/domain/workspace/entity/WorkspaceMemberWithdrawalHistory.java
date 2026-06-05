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
	name = "workspace_member_withdrawal_histories",
	indexes = {
		@Index(name = "idx_workspace_member_withdrawal_histories_workspace_id", columnList = "workspace_id"),
		@Index(name = "idx_workspace_member_withdrawal_histories_member_id", columnList = "workspace_member_id"),
		@Index(name = "idx_workspace_member_withdrawal_histories_user_id", columnList = "user_id"),
		@Index(name = "idx_workspace_member_withdrawal_histories_withdrawn_at", columnList = "withdrawn_at")
	}
)
public class WorkspaceMemberWithdrawalHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "workspace_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_workspace_member_withdrawal_histories_workspace")
	)
	private Workspace workspace;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "workspace_member_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_workspace_member_withdrawal_histories_member")
	)
	private WorkspaceMember workspaceMember;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "user_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_workspace_member_withdrawal_histories_user")
	)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_snapshot", nullable = false, length = 30)
	private WorkspaceMemberRole roleSnapshot;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "ownership_transferred_to_workspace_member_id",
		foreignKey = @ForeignKey(name = "fk_workspace_member_withdrawal_histories_transferred_member")
	)
	private WorkspaceMember ownershipTransferredToWorkspaceMember;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "ownership_transferred_to_user_id",
		foreignKey = @ForeignKey(name = "fk_workspace_member_withdrawal_histories_transferred_user")
	)
	private User ownershipTransferredToUser;

	@Column(name = "withdrawn_at", nullable = false)
	private Long withdrawnAt;
}
