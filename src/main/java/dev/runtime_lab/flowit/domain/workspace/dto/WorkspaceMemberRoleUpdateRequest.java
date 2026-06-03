package dev.runtime_lab.flowit.domain.workspace.dto;

import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMemberRole;
import jakarta.validation.constraints.NotNull;

public record WorkspaceMemberRoleUpdateRequest(
	@NotNull
	WorkspaceMemberRole role
) {
}
