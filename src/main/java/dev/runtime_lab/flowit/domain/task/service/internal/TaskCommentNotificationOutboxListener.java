package dev.runtime_lab.flowit.domain.task.service.internal;

import dev.runtime_lab.flowit.domain.task.event.TaskCommentCreatedEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventType;
import dev.runtime_lab.flowit.global.outbox.service.OutboxEventPublisher;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@InternalService
@RequiredArgsConstructor
public class TaskCommentNotificationOutboxListener {

	private final OutboxEventPublisher outboxEventPublisher;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void publishTaskCommentNotificationOutbox(TaskCommentCreatedEvent event) {
		outboxEventPublisher.publish(
			OutboxEventType.TASK_COMMENT_NOTIFICATION_REQUESTED,
			TaskCommentNotificationOutboxPayload.from(event)
		);
	}
}
