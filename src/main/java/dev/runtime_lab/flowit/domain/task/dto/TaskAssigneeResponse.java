package dev.runtime_lab.flowit.domain.task.dto;

import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMember;

public record TaskAssigneeResponse(
	Long memberId,
	Long userId,
	String name,
	String email
) {

	public static TaskAssigneeResponse from(WorkspaceMember workspaceMember) {
		if (workspaceMember == null) {
			return null;
		}

		return new TaskAssigneeResponse(
			workspaceMember.getId(),
			workspaceMember.getUser().getId(),
			workspaceMember.getUser().getName(),
			workspaceMember.getUser().getEmail()
		);
	}
}
