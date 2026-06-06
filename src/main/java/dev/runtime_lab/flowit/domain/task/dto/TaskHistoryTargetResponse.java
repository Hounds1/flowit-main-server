package dev.runtime_lab.flowit.domain.task.dto;

import dev.runtime_lab.flowit.domain.task.entity.TaskChangeHistory;

public record TaskHistoryTargetResponse(
	String type,
	Long taskId,
	String displayName
) {

	public static TaskHistoryTargetResponse from(TaskChangeHistory history) {
		return new TaskHistoryTargetResponse("TASK", history.getTask().getId(), history.getTaskTitleSnapshot());
	}
}
