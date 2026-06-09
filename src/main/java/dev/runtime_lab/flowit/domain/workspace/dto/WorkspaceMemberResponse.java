package dev.runtime_lab.flowit.domain.workspace.dto;

import dev.runtime_lab.flowit.domain.user.entity.UserStatus;
import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMemberRole;

public record WorkspaceMemberResponse(
	Long memberId,
	String name,
	String email,
	UserStatus status,
	WorkspaceMemberRole role,
	String profileImageUrl
) {

	private static final String PROFILE_IMAGE_URL = "/v1/workspaces/%d/members/%d/profile-image";

	public WorkspaceMemberResponse(
		Long memberId,
		String name,
		String email,
		UserStatus status,
		WorkspaceMemberRole role
	) {
		this(memberId, name, email, status, role, null);
	}

	public WorkspaceMemberResponse(
		Long workspaceId,
		Long memberId,
		String name,
		String email,
		UserStatus status,
		WorkspaceMemberRole role,
		Long profileImageFileId
	) {
		this(memberId, name, email, status, role, profileImageUrl(workspaceId, memberId, profileImageFileId));
	}

	public static String profileImageUrl(Long workspaceId, Long memberId, Long profileImageFileId) {
		return profileImageFileId == null ? null : PROFILE_IMAGE_URL.formatted(workspaceId, memberId);
	}
}
