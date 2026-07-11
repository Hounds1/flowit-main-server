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
import dev.runtime_lab.flowit.global.outbox.service.OutboxEventHandler;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@InternalService
@RequiredArgsConstructor
public class TaskCommentNotificationOutboxHandler implements OutboxEventHandler {

	private static final String EMPTY_CHANGES_JSON = "[]";

	private final NotificationAlertCreateService notificationAlertCreateService;
	private final JsonMapper jsonMapper;

	@Override
	public OutboxEventType supports() {
		return OutboxEventType.TASK_COMMENT_NOTIFICATION_REQUESTED;
	}

	@Override
	public void handle(OutboxEvent event) {
		TaskCommentNotificationOutboxPayload payload = readPayload(event.getPayloadJson());
		notificationAlertCreateService.create(command(payload));
	}

	private NotificationAlertCreateCommand command(TaskCommentNotificationOutboxPayload payload) {
		return new NotificationAlertCreateCommand(
			NotificationSourceType.TASK_COMMENT,
			payload.commentId(),
			NotificationAlertType.TASK_COMMENT_CREATED,
			NotificationScopeType.WORKSPACE,
			payload.workspaceId(),
			payload.workspaceName(),
			NotificationActorType.USER,
			payload.actorUserId(),
			payload.actorName(),
			NotificationSubjectType.TASK,
			payload.taskId(),
			payload.taskTitle(),
			EMPTY_CHANGES_JSON,
			NotificationLinkType.TASK_DETAIL,
			payload.workspaceId(),
			payload.occurredAt(),
			recipientUserIds(payload)
		);
	}

	private List<Long> recipientUserIds(TaskCommentNotificationOutboxPayload payload) {
		Set<Long> recipientUserIds = new LinkedHashSet<>();
		recipientUserIds.add(payload.taskCreatorUserId());
		recipientUserIds.add(payload.taskAssigneeUserId());
		recipientUserIds.remove(null);
		recipientUserIds.remove(payload.actorUserId());
		return List.copyOf(recipientUserIds);
	}

	private TaskCommentNotificationOutboxPayload readPayload(String payloadJson) {
		try {
			return jsonMapper.readValue(payloadJson, TaskCommentNotificationOutboxPayload.class);
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Failed to deserialize task comment notification outbox payload.", exception);
		}
	}
}
