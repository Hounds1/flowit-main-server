package dev.runtime_lab.flowit.domain.task.dto;

public record TaskIndicatorResponse(
	long total,
	long inProgress,
	long dueToday,
	long expired
) {
}
