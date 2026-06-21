package dev.runtime_lab.flowit.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	name = "notification_recipients",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_notification_recipients_alert_user",
			columnNames = {"notification_alert_id", "user_id"}
		)
	},
	indexes = {
		@Index(name = "idx_notification_recipients_user_created", columnList = "user_id, created_at, id"),
		@Index(name = "idx_notification_recipients_user_seen", columnList = "user_id, seen_at"),
		@Index(name = "idx_notification_recipients_user_read", columnList = "user_id, read_at"),
		@Index(name = "idx_notification_recipients_alert", columnList = "notification_alert_id")
	}
)
public class NotificationRecipient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
		name = "notification_alert_id",
		nullable = false,
		foreignKey = @ForeignKey(name = "fk_notification_recipients_alert")
	)
	private NotificationAlert notificationAlert;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "created_at", nullable = false)
	private Long createdAt;

	@Column(name = "seen_at")
	private Long seenAt;

	@Column(name = "read_at")
	private Long readAt;

	@Column(name = "hidden_at")
	private Long hiddenAt;
}
