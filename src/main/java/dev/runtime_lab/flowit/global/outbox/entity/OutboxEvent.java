package dev.runtime_lab.flowit.global.outbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
	name = "outbox_events",
	indexes = {
		@Index(name = "idx_outbox_events_status_created", columnList = "status, created_at, id"),
		@Index(name = "idx_outbox_events_type", columnList = "event_type")
	}
)
public class OutboxEvent {

	private static final int FAILURE_MESSAGE_LIMIT = 500;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 80)
	private OutboxEventType eventType;

	@Column(name = "payload_json", nullable = false, columnDefinition = "LONGTEXT")
	private String payloadJson;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	@Builder.Default
	private OutboxEventStatus status = OutboxEventStatus.PENDING;

	@Column(name = "created_at", nullable = false)
	private Long createdAt;

	@Column(name = "updated_at", nullable = false)
	private Long updatedAt;

	@Column(name = "processed_at")
	private Long processedAt;

	@Column(name = "failed_at")
	private Long failedAt;

	@Column(name = "failure_message", length = FAILURE_MESSAGE_LIMIT)
	private String failureMessage;

	public void markProcessed(Long processedAt) {
		this.status = OutboxEventStatus.PROCESSED;
		this.processedAt = processedAt;
		this.failedAt = null;
		this.failureMessage = null;
		this.updatedAt = processedAt;
	}

	public void markFailed(Long failedAt, String failureMessage) {
		this.status = OutboxEventStatus.FAILED;
		this.failedAt = failedAt;
		this.failureMessage = truncate(failureMessage);
		this.updatedAt = failedAt;
	}

	private String truncate(String value) {
		if (value == null || value.length() <= FAILURE_MESSAGE_LIMIT) {
			return value;
		}

		return value.substring(0, FAILURE_MESSAGE_LIMIT);
	}
}
