package dev.runtime_lab.flowit.domain.task.dto;

import dev.runtime_lab.flowit.domain.task.entity.TaskHistoryElement;

public record TaskHistoryChangeResponse(
	TaskHistoryElement element,
	Object from,
	Object to
) {
}
