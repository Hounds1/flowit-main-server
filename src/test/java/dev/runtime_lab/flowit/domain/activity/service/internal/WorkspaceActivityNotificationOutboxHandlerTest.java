package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;
import dev.runtime_lab.flowit.domain.activity.repository.WorkspaceActivityRecordRepository;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertCreateService;
import dev.runtime_lab.flowit.domain.notification.service.internal.command.NotificationAlertCreateCommand;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkspaceActivityNotificationOutboxHandlerTest {

	private final WorkspaceActivityRecordRepository workspaceActivityRecordRepository =
		mock(WorkspaceActivityRecordRepository.class);
	private final WorkspaceActivityNotificationCommandFactory notificationCommandFactory =
		mock(WorkspaceActivityNotificationCommandFactory.class);
	private final NotificationAlertCreateService notificationAlertCreateService =
		mock(NotificationAlertCreateService.class);
	private final WorkspaceActivityNotificationOutboxHandler handler =
		new WorkspaceActivityNotificationOutboxHandler(
			workspaceActivityRecordRepository,
			List.of(notificationCommandFactory),
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
		NotificationAlertCreateCommand firstCommand = command(NotificationAlertType.TASK_ASSIGNED);
		NotificationAlertCreateCommand secondCommand = command(NotificationAlertType.TASK_STATUS_CHANGED);
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

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<NotificationAlertCreateCommand>> commandsCaptor = ArgumentCaptor.forClass(List.class);
		verify(notificationAlertCreateService).createAll(commandsCaptor.capture());
		assertEquals(List.of(firstCommand, secondCommand), commandsCaptor.getValue());
	}

	@Test
	void filtersCurrentlyDisabledTaskNotificationsBeforeCreateService() {
		WorkspaceActivityRecord record = WorkspaceActivityRecord.builder().id(921L).build();
		NotificationAlertCreateCommand dateCommand = command(NotificationAlertType.TASK_DATE_CHANGED);
		NotificationAlertCreateCommand statusCommand = command(NotificationAlertType.TASK_STATUS_CHANGED);
		NotificationAlertCreateCommand progressCommand = command(NotificationAlertType.TASK_PROGRESS_CHANGED);
		OutboxEvent event = outboxEvent();

		when(workspaceActivityRecordRepository.findByIdWithWorkspaceAndActor(921L))
			.thenReturn(Optional.of(record));
		when(notificationCommandFactory.create(record)).thenReturn(List.of(dateCommand, statusCommand, progressCommand));

		handler.handle(event);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<NotificationAlertCreateCommand>> commandsCaptor = ArgumentCaptor.forClass(List.class);
		verify(notificationAlertCreateService).createAll(commandsCaptor.capture());
		assertEquals(List.of(statusCommand), commandsCaptor.getValue());
	}

	@Test
	void doesNotCallCreateServiceWhenOnlyDisabledTaskNotificationsRemain() {
		WorkspaceActivityRecord record = WorkspaceActivityRecord.builder().id(921L).build();
		NotificationAlertCreateCommand dateCommand = command(NotificationAlertType.TASK_DATE_CHANGED);
		NotificationAlertCreateCommand progressCommand = command(NotificationAlertType.TASK_PROGRESS_CHANGED);
		OutboxEvent event = outboxEvent();

		when(workspaceActivityRecordRepository.findByIdWithWorkspaceAndActor(921L))
			.thenReturn(Optional.of(record));
		when(notificationCommandFactory.create(record)).thenReturn(List.of(dateCommand, progressCommand));

		handler.handle(event);

		verify(notificationAlertCreateService, never()).createAll(any());
	}

	private NotificationAlertCreateCommand command(NotificationAlertType type) {
		NotificationAlertCreateCommand command = mock(NotificationAlertCreateCommand.class);
		when(command.type()).thenReturn(type);
		return command;
	}

	private OutboxEvent outboxEvent() {
		return OutboxEvent.builder()
			.eventType(OutboxEventType.WORKSPACE_ACTIVITY_NOTIFICATION_REQUESTED)
			.payloadJson("""
				{"activityRecordId":921}
				""")
			.createdAt(1782013300L)
			.updatedAt(1782013300L)
			.build();
	}
}
