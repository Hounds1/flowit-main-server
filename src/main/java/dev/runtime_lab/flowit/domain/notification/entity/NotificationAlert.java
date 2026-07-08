package dev.runtime_lab.flowit.domain.notification.entity;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
	name = "notification_alerts",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_notification_alerts_source",
			columnNames = {"source_type", "source_id", "type"}
		)
	},
	indexes = {
		@Index(name = "idx_notification_alerts_scope_created", columnList = "scope_type, scope_id, created_at, id"),
		@Index(name = "idx_notification_alerts_type", columnList = "type"),
		@Index(name = "idx_notification_alerts_occurred", columnList = "occurred_at")
	}
)
public class NotificationAlert {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", nullable = false, length = 60)
	private NotificationSourceType sourceType;

	@Column(name = "source_id", nullable = false)
	private Long sourceId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 80)
	private NotificationAlertType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "scope_type", nullable = false, length = 40)
	private NotificationScopeType scopeType;

	@Column(name = "scope_id", nullable = false)
	private Long scopeId;

	@Column(name = "scope_name_snapshot", nullable = false, length = 100)
	private String scopeNameSnapshot;

	@Enumerated(EnumType.STRING)
	@Column(name = "actor_type", length = 40)
	private NotificationActorType actorType;

	@Column(name = "actor_id")
	private Long actorId;

	@Column(name = "actor_name_snapshot", length = 100)
	private String actorNameSnapshot;

	@Enumerated(EnumType.STRING)
	@Column(name = "subject_type", nullable = false, length = 40)
	private NotificationSubjectType subjectType;

	@Column(name = "subject_id", nullable = false)
	private Long subjectId;

	@Column(name = "subject_name_snapshot", nullable = false, length = 100)
	private String subjectNameSnapshot;

	@Column(name = "changes_json", nullable = false, columnDefinition = "LONGTEXT")
	private String changesJson;

	@Enumerated(EnumType.STRING)
	@Column(name = "link_type", nullable = false, length = 40)
	private NotificationLinkType linkType;

	@Column(name = "link_workspace_id")
	private Long linkWorkspaceId;

	@Column(name = "occurred_at", nullable = false)
	private Long occurredAt;

	@Column(name = "group_id", length = 120)
	private String groupId;

	@Builder.Default
	@Column(name = "group_sequence", nullable = false)
	private Integer groupSequence = 0;

	@Column(name = "created_at", nullable = false)
	private Long createdAt;
}
