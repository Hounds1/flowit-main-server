package dev.runtime_lab.flowit.domain.task.repository.projection;

public record TaskIndicatorCounts(
	long total,
	long inProgress,
	long dueToday,
	long expired
) {

	public static TaskIndicatorCounts empty() {
		return new TaskIndicatorCounts(0L, 0L, 0L, 0L);
	}
}
