package dev.runtime_lab.flowit.domain.user.service.internal;

import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.user.entity.UserStatus;
import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserAuthenticationServiceTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final UserAuthenticationService userAuthenticationService = new UserAuthenticationService(userRepository);

	@Test
	void findActiveByEmailReturnsActiveUser() {
		User user = user(UserStatus.ACTIVE, null);

		when(userRepository.findActiveByEmail("user@example.com")).thenReturn(Optional.of(user));

		assertEquals(Optional.of(user), userAuthenticationService.findActiveByEmail("user@example.com"));
	}

	@Test
	void findActiveByEmailRejectsInactiveUser() {
		User user = user(UserStatus.LOCKED, null);

		when(userRepository.findActiveByEmail("user@example.com")).thenReturn(Optional.of(user));

		assertTrue(userAuthenticationService.findActiveByEmail("user@example.com").isEmpty());
	}

	@Test
	void findActiveByIdRejectsDeletedUserDefensively() {
		User user = user(UserStatus.ACTIVE, 1_780_142_400L);

		when(userRepository.findActiveById(1001L)).thenReturn(Optional.of(user));

		assertTrue(userAuthenticationService.findActiveById(1001L).isEmpty());
	}

	private User user(UserStatus status, Long deletedAt) {
		return User.builder()
			.id(1001L)
			.email("user@example.com")
			.passwordHash("encodedPassword")
			.name("nickname")
			.status(status)
			.createdAt(1779888000L)
			.updatedAt(1779888000L)
			.deletedAt(deletedAt)
			.build();
	}
}
