package dev.runtime_lab.flowit.domain.task.dto;

import dev.runtime_lab.flowit.domain.task.entity.TaskChangeHistory;

public record TaskHistoryActorResponse(
	Long memberId,
	Long userId,
	String displayName
) {

	public static TaskHistoryActorResponse from(TaskChangeHistory history) {
		Long memberId = history.getActorWorkspaceMember() == null ? null : history.getActorWorkspaceMember().getId();
		Long userId = history.getActorUser() == null ? null : history.getActorUser().getId();

		return new TaskHistoryActorResponse(memberId, userId, history.getActorDisplayNameSnapshot());
	}
}
