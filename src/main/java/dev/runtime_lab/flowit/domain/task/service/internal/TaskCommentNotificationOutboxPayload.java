package dev.runtime_lab.flowit.domain.task.service.internal;

import dev.runtime_lab.flowit.domain.task.event.TaskCommentCreatedEvent;

public record TaskCommentNotificationOutboxPayload(
	Long commentId,
	Long workspaceId,
	String workspaceName,
	Long taskId,
	String taskTitle,
	Long actorUserId,
	String actorName,
	Long taskCreatorUserId,
	Long taskAssigneeUserId,
	Long occurredAt
) {

	public static TaskCommentNotificationOutboxPayload from(TaskCommentCreatedEvent event) {
		return new TaskCommentNotificationOutboxPayload(
			event.commentId(),
			event.workspaceId(),
			event.workspaceName(),
			event.taskId(),
			event.taskTitle(),
			event.actorUserId(),
			event.actorName(),
			event.taskCreatorUserId(),
			event.taskAssigneeUserId(),
			event.occurredAt()
		);
	}
}
