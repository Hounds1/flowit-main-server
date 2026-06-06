package dev.runtime_lab.flowit.domain.task.dto;

import dev.runtime_lab.flowit.domain.task.entity.Task;

public record TaskCreateResponse(
	Long id,
	Long createdAt
) {

	public static TaskCreateResponse from(Task task) {
		return new TaskCreateResponse(task.getId(), task.getCreatedAt());
	}
}
