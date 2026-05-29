package dev.runtime_lab.flowit.domain.user.dto;

import dev.runtime_lab.flowit.domain.user.entity.UserStatus;
import java.util.List;

public record UserMeResponse(
	Long id,
	String email,
	String nickname,
	UserStatus status,
	Long profileImageFileId,
	List<UserMeWorkspaceResponse> workspaces
) {
}
