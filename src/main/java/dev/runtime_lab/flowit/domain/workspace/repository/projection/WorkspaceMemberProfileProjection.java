package dev.runtime_lab.flowit.domain.workspace.repository.projection;

public record WorkspaceMemberProfileProjection(
	Long memberId,
	Long userId,
	String name,
	Long profileImageFileId
) {
}
