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
	name = "workspace_members",
	indexes = {
		@Index(name = "idx_workspace_members_workspace_id", columnList = "workspace_id"),
		@Index(name = "idx_workspace_members_user_id", columnList = "user_id"),
		@Index(name = "idx_workspace_members_role", columnList = "role"),
		@Index(name = "idx_workspace_members_deleted_at", columnList = "deleted_at")
	}
)
public class WorkspaceMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "workspace_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_workspace_members_workspace")
	)
	private Workspace workspace;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "user_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_workspace_members_user")
	)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 30)
	@Builder.Default
	private WorkspaceMemberRole role = WorkspaceMemberRole.MEMBER;

	@Column(name = "joined_at", nullable = false)
	private Long joinedAt;

	@Column(name = "created_at", nullable = false)
	private Long createdAt;

	@Column(name = "updated_at", nullable = false)
	private Long updatedAt;

	@Column(name = "deleted_at")
	private Long deletedAt;
}
