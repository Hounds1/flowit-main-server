package dev.runtime_lab.flowit.domain.task.event;

import dev.runtime_lab.flowit.domain.task.entity.Task;
import dev.runtime_lab.flowit.domain.task.entity.TaskComment;
import java.util.Objects;

public record TaskCommentCreatedEvent(
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

	public static TaskCommentCreatedEvent from(TaskComment comment) {
		Objects.requireNonNull(comment, "comment must not be null");
		Task task = comment.getTask();

		return new TaskCommentCreatedEvent(
			comment.getId(),
			comment.getWorkspace().getId(),
			comment.getWorkspace().getName(),
			task.getId(),
			task.getTitle(),
			comment.getAuthorUser().getId(),
			comment.getAuthorDisplayNameSnapshot(),
			task.getCreatedBy().getId(),
			task.getAssignee() == null ? null : task.getAssignee().getUser().getId(),
			comment.getCreatedAt()
		);
	}
}
