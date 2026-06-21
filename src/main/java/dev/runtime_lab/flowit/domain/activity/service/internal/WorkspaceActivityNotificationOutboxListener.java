package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.event.WorkspaceActivityRecordedEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventType;
import dev.runtime_lab.flowit.global.outbox.service.OutboxEventPublisher;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@InternalService
@RequiredArgsConstructor
public class WorkspaceActivityNotificationOutboxListener {

	private final OutboxEventPublisher outboxEventPublisher;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void publishWorkspaceMemberNotificationOutbox(WorkspaceActivityRecordedEvent event) {
		if (event.activityRecordId() == null || event.domain() != ActivityRecordDomain.WORKSPACE_MEMBER) {
			return;
		}

		outboxEventPublisher.publish(
			OutboxEventType.WORKSPACE_ACTIVITY_NOTIFICATION_REQUESTED,
			new WorkspaceActivityNotificationOutboxPayload(event.activityRecordId())
		);
	}
}
