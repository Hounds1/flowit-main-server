package dev.runtime_lab.flowit.domain.user.service.internal;

import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.user.entity.UserStatus;
import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import dev.runtime_lab.flowit.global.security.authentication.InvalidAuthenticatedUserException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrentUserProviderTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final CurrentUserProvider currentUserProvider = new CurrentUserProvider(userRepository);

	@Test
	void findActiveReturnsActiveCurrentUser() {
		CurrentUser currentUser = new CurrentUser(1L, "user@example.com", "nickname");
		User user = activeUser(UserStatus.ACTIVE);

		when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));

		User found = currentUserProvider.findActive(currentUser);

		assertSame(user, found);
		verify(userRepository).findActiveById(1L);
	}

	@Test
	void findActiveRejectsMissingUser() {
		CurrentUser currentUser = new CurrentUser(1L, "user@example.com", "nickname");

		when(userRepository.findActiveById(1L)).thenReturn(Optional.empty());

		assertThrows(InvalidAuthenticatedUserException.class,
			() -> currentUserProvider.findActive(currentUser));
	}

	@Test
	void findActiveRejectsInactiveUser() {
		CurrentUser currentUser = new CurrentUser(1L, "user@example.com", "nickname");

		when(userRepository.findActiveById(1L)).thenReturn(Optional.of(activeUser(UserStatus.LOCKED)));

		assertThrows(InvalidAuthenticatedUserException.class,
			() -> currentUserProvider.findActive(currentUser));
	}

	@Test
	void findActiveForUpdateReturnsLockedActiveCurrentUser() {
		CurrentUser currentUser = new CurrentUser(1L, "user@example.com", "nickname");
		User user = activeUser(UserStatus.ACTIVE);

		when(userRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(user));

		User found = currentUserProvider.findActiveForUpdate(currentUser);

		assertSame(user, found);
		verify(userRepository).findActiveByIdForUpdate(1L);
	}

	@Test
	void findActiveForUpdateRejectsMissingUser() {
		CurrentUser currentUser = new CurrentUser(1L, "user@example.com", "nickname");

		when(userRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.empty());

		assertThrows(InvalidAuthenticatedUserException.class,
			() -> currentUserProvider.findActiveForUpdate(currentUser));
	}

	@Test
	void findActiveForUpdateRejectsInactiveUser() {
		CurrentUser currentUser = new CurrentUser(1L, "user@example.com", "nickname");

		when(userRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(activeUser(UserStatus.LOCKED)));

		assertThrows(InvalidAuthenticatedUserException.class,
			() -> currentUserProvider.findActiveForUpdate(currentUser));
	}

	private User activeUser(UserStatus status) {
		return User.builder()
			.id(1L)
			.email("user@example.com")
			.passwordHash("hash")
			.name("nickname")
			.status(status)
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}
}
