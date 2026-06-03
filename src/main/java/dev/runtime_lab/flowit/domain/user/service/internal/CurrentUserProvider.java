package dev.runtime_lab.flowit.domain.user.service.internal;

import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.user.entity.UserStatus;
import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import dev.runtime_lab.flowit.global.security.authentication.InvalidAuthenticatedUserException;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@InternalService
@RequiredArgsConstructor
public class CurrentUserProvider {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public User findActive(CurrentUser currentUser) {
		return userRepository.findActiveById(currentUser.id())
			.filter(user -> user.getStatus() == UserStatus.ACTIVE)
			.orElseThrow(InvalidAuthenticatedUserException::new);
	}

	@Transactional
	public User findActiveForUpdate(CurrentUser currentUser) {
		return userRepository.findActiveByIdForUpdate(currentUser.id())
			.filter(user -> user.getStatus() == UserStatus.ACTIVE)
			.orElseThrow(InvalidAuthenticatedUserException::new);
	}
}
