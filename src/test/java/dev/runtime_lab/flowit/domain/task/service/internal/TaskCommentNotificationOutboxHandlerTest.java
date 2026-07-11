package dev.runtime_lab.flowit.domain.task.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationSourceType;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertCreateService;
import dev.runtime_lab.flowit.domain.notification.service.internal.command.NotificationAlertCreateCommand;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TaskCommentNotificationOutboxHandlerTest {

	private final NotificationAlertCreateService notificationAlertCreateService =
		mock(NotificationAlertCreateService.class);
	private final TaskCommentNotificationOutboxHandler handler = new TaskCommentNotificationOutboxHandler(
		notificationAlertCreateService,
		JsonMapper.builder().build()
	);

	@Test
	void supportsTaskCommentNotificationRequested() {
		assertEquals(OutboxEventType.TASK_COMMENT_NOTIFICATION_REQUESTED, handler.supports());
	}

	@Test
	void createsTaskCommentNotificationForCreatorAndAssignee() {
		handler.handle(outboxEvent(34L, 35L, 36L));

		ArgumentCaptor<NotificationAlertCreateCommand> commandCaptor =
			ArgumentCaptor.forClass(NotificationAlertCreateCommand.class);
		verify(notificationAlertCreateService).create(commandCaptor.capture());
		NotificationAlertCreateCommand command = commandCaptor.getValue();
		assertEquals(NotificationSourceType.TASK_COMMENT, command.sourceType());
		assertEquals(500L, command.sourceId());
		assertEquals(NotificationAlertType.TASK_COMMENT_CREATED, command.type());
		assertEquals(NotificationScopeType.WORKSPACE, command.scopeType());
		assertEquals(12L, command.scopeId());
		assertEquals("Flowit", command.scopeName());
		assertEquals(NotificationActorType.USER, command.actorType());
		assertEquals(34L, command.actorId());
		assertEquals("Actor", command.actorName());
		assertEquals(NotificationSubjectType.TASK, command.subjectType());
		assertEquals(1001L, command.subjectId());
		assertEquals("Login UI", command.subjectName());
		assertEquals("[]", command.changesJson());
		assertEquals(NotificationLinkType.TASK_DETAIL, command.linkType());
		assertEquals(12L, command.linkWorkspaceId());
		assertEquals(1782013200L, command.occurredAt());
		assertEquals(List.of(35L, 36L), command.recipientUserIds());
	}

	@Test
	void excludesActorAndDeduplicatesCreatorAndAssignee() {
		handler.handle(outboxEvent(34L, 35L, 35L));

		ArgumentCaptor<NotificationAlertCreateCommand> commandCaptor =
			ArgumentCaptor.forClass(NotificationAlertCreateCommand.class);
		verify(notificationAlertCreateService).create(commandCaptor.capture());
		assertEquals(List.of(35L), commandCaptor.getValue().recipientUserIds());
	}

	@Test
	void excludesActorWhenActorIsTaskCreator() {
		handler.handle(outboxEvent(34L, 34L, 36L));

		ArgumentCaptor<NotificationAlertCreateCommand> commandCaptor =
			ArgumentCaptor.forClass(NotificationAlertCreateCommand.class);
		verify(notificationAlertCreateService).create(commandCaptor.capture());
		assertEquals(List.of(36L), commandCaptor.getValue().recipientUserIds());
	}

	@Test
	void usesOnlyCreatorWhenTaskHasNoAssignee() {
		handler.handle(outboxEvent(34L, 35L, null));

		ArgumentCaptor<NotificationAlertCreateCommand> commandCaptor =
			ArgumentCaptor.forClass(NotificationAlertCreateCommand.class);
		verify(notificationAlertCreateService).create(commandCaptor.capture());
		assertEquals(List.of(35L), commandCaptor.getValue().recipientUserIds());
	}

	@Test
	void createsCommandWithNoRecipientsWhenActorOwnsAndIsAssignedToTask() {
		handler.handle(outboxEvent(34L, 34L, 34L));

		ArgumentCaptor<NotificationAlertCreateCommand> commandCaptor =
			ArgumentCaptor.forClass(NotificationAlertCreateCommand.class);
		verify(notificationAlertCreateService).create(commandCaptor.capture());
		assertEquals(List.of(), commandCaptor.getValue().recipientUserIds());
	}

	private OutboxEvent outboxEvent(Long actorUserId, Long creatorUserId, Long assigneeUserId) {
		return OutboxEvent.builder()
			.eventType(OutboxEventType.TASK_COMMENT_NOTIFICATION_REQUESTED)
			.payloadJson("""
				{
				  "commentId": 500,
				  "workspaceId": 12,
				  "workspaceName": "Flowit",
				  "taskId": 1001,
				  "taskTitle": "Login UI",
				  "actorUserId": %d,
				  "actorName": "Actor",
				  "taskCreatorUserId": %d,
				  "taskAssigneeUserId": %d,
				  "occurredAt": 1782013200
				}
				""".formatted(actorUserId, creatorUserId, assigneeUserId))
			.createdAt(1782013300L)
			.updatedAt(1782013300L)
			.build();
	}
}
