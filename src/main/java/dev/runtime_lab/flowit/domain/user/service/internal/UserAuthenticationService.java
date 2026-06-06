package dev.runtime_lab.flowit.domain.user.service.internal;

import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.user.entity.UserStatus;
import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@InternalService
@RequiredArgsConstructor
public class UserAuthenticationService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public Optional<User> findActiveByEmail(String email) {
		return userRepository.findActiveByEmail(email)
			.filter(this::isAuthenticatable);
	}

	@Transactional(readOnly = true)
	public Optional<User> findActiveById(Long userId) {
		return userRepository.findActiveById(userId)
			.filter(this::isAuthenticatable);
	}

	private boolean isAuthenticatable(User user) {
		return user.getDeletedAt() == null && user.getStatus() == UserStatus.ACTIVE;
	}
}
