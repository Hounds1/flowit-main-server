package dev.runtime_lab.flowit.domain.user.dto;

import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.user.entity.UserStatus;

public record JoinResponse(
	Long id,
	String email,
	String nickname,
	UserStatus status,
	Long createdAt
) {

	public static JoinResponse from(User user) {
		return new JoinResponse(
			user.getId(),
			user.getEmail(),
			user.getName(),
			user.getStatus(),
			user.getCreatedAt()
		);
	}
}
