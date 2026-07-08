package dev.runtime_lab.flowit.domain.user.repository.projection;

public record UserProfileProjection(
	Long userId,
	String name,
	Long profileImageFileId
) {
}
