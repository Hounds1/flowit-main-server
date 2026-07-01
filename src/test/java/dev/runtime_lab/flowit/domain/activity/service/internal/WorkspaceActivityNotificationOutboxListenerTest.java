package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordAction;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.event.WorkspaceActivityRecordedEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventType;
import dev.runtime_lab.flowit.global.outbox.service.OutboxEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class WorkspaceActivityNotificationOutboxListenerTest {

	private final OutboxEventPublisher outboxEventPublisher = mock(OutboxEventPublisher.class);
	private final WorkspaceActivityNotificationOutboxListener listener =
		new WorkspaceActivityNotificationOutboxListener(outboxEventPublisher);

	@Test
	void publishesOutboxEventForWorkspaceMemberActivityBeforeCommit() {
		ArgumentCaptor<WorkspaceActivityNotificationOutboxPayload> payloadCaptor =
			ArgumentCaptor.forClass(WorkspaceActivityNotificationOutboxPayload.class);

		listener.publishWorkspaceActivityNotificationOutbox(new WorkspaceActivityRecordedEvent(
			921L,
			12L,
			ActivityRecordDomain.WORKSPACE_MEMBER,
			ActivityRecordAction.JOINED,
			1782013200L
		));

		verify(outboxEventPublisher).publish(
			org.mockito.ArgumentMatchers.eq(OutboxEventType.WORKSPACE_ACTIVITY_NOTIFICATION_REQUESTED),
			payloadCaptor.capture()
		);
		assertEquals(921L, payloadCaptor.getValue().activityRecordId());
	}

	@Test
	void publishesOutboxEventForTaskActivityBeforeCommit() {
		ArgumentCaptor<WorkspaceActivityNotificationOutboxPayload> payloadCaptor =
			ArgumentCaptor.forClass(WorkspaceActivityNotificationOutboxPayload.class);

		listener.publishWorkspaceActivityNotificationOutbox(new WorkspaceActivityRecordedEvent(
			1L,
			12L,
			ActivityRecordDomain.TASK,
			ActivityRecordAction.CREATED,
			1782013200L
		));

		verify(outboxEventPublisher).publish(
			org.mockito.ArgumentMatchers.eq(OutboxEventType.WORKSPACE_ACTIVITY_NOTIFICATION_REQUESTED),
			payloadCaptor.capture()
		);
		assertEquals(1L, payloadCaptor.getValue().activityRecordId());
	}

	@Test
	void ignoresActivityWithoutRecordId() {
		listener.publishWorkspaceActivityNotificationOutbox(new WorkspaceActivityRecordedEvent(
			null,
			12L,
			ActivityRecordDomain.TASK,
			ActivityRecordAction.CREATED,
			1782013200L
		));

		verify(outboxEventPublisher, never()).publish(
			org.mockito.ArgumentMatchers.any(),
			org.mockito.ArgumentMatchers.any()
		);
	}
}
