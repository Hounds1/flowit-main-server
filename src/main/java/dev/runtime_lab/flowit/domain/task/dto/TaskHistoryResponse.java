package dev.runtime_lab.flowit.domain.task.dto;

import dev.runtime_lab.flowit.domain.task.entity.TaskChangeHistory;
import dev.runtime_lab.flowit.domain.task.entity.TaskHistoryAction;
import java.util.List;

public record TaskHistoryResponse(
	Long id,
	Long occurredAt,
	TaskHistoryActorResponse actor,
	TaskHistoryTargetResponse target,
	TaskHistoryAction action,
	List<TaskHistoryChangeResponse> changes
) {

	public static TaskHistoryResponse from(TaskChangeHistory history, List<TaskHistoryChangeResponse> changes) {
		return new TaskHistoryResponse(
			history.getId(),
			history.getChangedAt(),
			TaskHistoryActorResponse.from(history),
			TaskHistoryTargetResponse.from(history),
			history.getAction(),
			List.copyOf(changes)
		);
	}
}
