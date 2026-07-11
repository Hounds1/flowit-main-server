package dev.runtime_lab.flowit.domain.task.service.internal;

import dev.runtime_lab.flowit.domain.task.event.TaskCommentCreatedEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventType;
import dev.runtime_lab.flowit.global.outbox.service.OutboxEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TaskCommentNotificationOutboxListenerTest {

	private final OutboxEventPublisher outboxEventPublisher = mock(OutboxEventPublisher.class);
	private final TaskCommentNotificationOutboxListener listener =
		new TaskCommentNotificationOutboxListener(outboxEventPublisher);

	@Test
	void publishesCommentNotificationSnapshotBeforeCommit() {
		TaskCommentCreatedEvent event = new TaskCommentCreatedEvent(
			500L,
			12L,
			"Flowit",
			1001L,
			"Login UI",
			34L,
			"Actor",
			35L,
			36L,
			1782013200L
		);
		ArgumentCaptor<TaskCommentNotificationOutboxPayload> payloadCaptor =
			ArgumentCaptor.forClass(TaskCommentNotificationOutboxPayload.class);

		listener.publishTaskCommentNotificationOutbox(event);

		verify(outboxEventPublisher).publish(
			eq(OutboxEventType.TASK_COMMENT_NOTIFICATION_REQUESTED),
			payloadCaptor.capture()
		);
		TaskCommentNotificationOutboxPayload payload = payloadCaptor.getValue();
		assertEquals(500L, payload.commentId());
		assertEquals(12L, payload.workspaceId());
		assertEquals("Flowit", payload.workspaceName());
		assertEquals(1001L, payload.taskId());
		assertEquals("Login UI", payload.taskTitle());
		assertEquals(34L, payload.actorUserId());
		assertEquals("Actor", payload.actorName());
		assertEquals(35L, payload.taskCreatorUserId());
		assertEquals(36L, payload.taskAssigneeUserId());
		assertEquals(1782013200L, payload.occurredAt());
	}
}
