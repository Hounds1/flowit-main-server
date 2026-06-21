package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;
import dev.runtime_lab.flowit.domain.activity.repository.WorkspaceActivityRecordRepository;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertCreateService;
import dev.runtime_lab.flowit.domain.notification.service.internal.command.NotificationAlertCreateCommand;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkspaceActivityNotificationOutboxHandlerTest {

	private final WorkspaceActivityRecordRepository workspaceActivityRecordRepository =
		mock(WorkspaceActivityRecordRepository.class);
	private final WorkspaceMemberActivityNotificationCommandFactory notificationCommandFactory =
		mock(WorkspaceMemberActivityNotificationCommandFactory.class);
	private final NotificationAlertCreateService notificationAlertCreateService =
		mock(NotificationAlertCreateService.class);
	private final WorkspaceActivityNotificationOutboxHandler handler =
		new WorkspaceActivityNotificationOutboxHandler(
			workspaceActivityRecordRepository,
			notificationCommandFactory,
			notificationAlertCreateService,
			JsonMapper.builder().build()
		);

	@Test
	void supportsWorkspaceActivityNotificationRequested() {
		assertEquals(OutboxEventType.WORKSPACE_ACTIVITY_NOTIFICATION_REQUESTED, handler.supports());
	}

	@Test
	void createsNotificationFromOutboxPayload() {
		WorkspaceActivityRecord record = WorkspaceActivityRecord.builder().id(921L).build();
		NotificationAlertCreateCommand firstCommand = mock(NotificationAlertCreateCommand.class);
		NotificationAlertCreateCommand secondCommand = mock(NotificationAlertCreateCommand.class);
		OutboxEvent event = OutboxEvent.builder()
			.eventType(OutboxEventType.WORKSPACE_ACTIVITY_NOTIFICATION_REQUESTED)
			.payloadJson("""
				{"activityRecordId":921}
				""")
			.createdAt(1782013300L)
			.updatedAt(1782013300L)
			.build();

		when(workspaceActivityRecordRepository.findByIdWithWorkspaceAndActor(921L))
			.thenReturn(Optional.of(record));
		when(notificationCommandFactory.create(record)).thenReturn(List.of(firstCommand, secondCommand));

		handler.handle(event);

		verify(notificationAlertCreateService).create(firstCommand);
		verify(notificationAlertCreateService).create(secondCommand);
	}
}
