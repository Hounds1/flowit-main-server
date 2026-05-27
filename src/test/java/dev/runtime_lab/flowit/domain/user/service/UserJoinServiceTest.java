package dev.runtime_lab.flowit.domain.user.service;

import dev.runtime_lab.flowit.domain.user.dto.JoinRequest;
import dev.runtime_lab.flowit.domain.user.dto.JoinResponse;
import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.user.entity.UserStatus;
import dev.runtime_lab.flowit.domain.user.exception.DuplicateActiveEmailException;
import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserJoinServiceTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final Clock clock = Clock.fixed(Instant.ofEpochSecond(1779889000L), ZoneOffset.UTC);
	private final UserJoinService userJoinService = new UserJoinService(userRepository, passwordEncoder, clock);

	@Test
	void joinCreatesActiveUserWithEncodedPassword() {
		JoinRequest request = new JoinRequest("user@example.com", "plainPassword", "nickname");
		User savedUser = User.builder()
			.id(1L)
			.email("user@example.com")
			.passwordHash("encodedPassword")
			.name("nickname")
			.createdAt(1779889000L)
			.updatedAt(1779889000L)
			.build();
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

		when(userRepository.existsActiveByEmail("user@example.com")).thenReturn(false);
		when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
		when(userRepository.save(userCaptor.capture())).thenReturn(savedUser);

		JoinResponse response = userJoinService.join(request);

		User userToSave = userCaptor.getValue();
		assertEquals("user@example.com", userToSave.getEmail());
		assertEquals("encodedPassword", userToSave.getPasswordHash());
		assertEquals("nickname", userToSave.getName());
		assertEquals(UserStatus.ACTIVE, userToSave.getStatus());
		assertEquals(1779889000L, userToSave.getCreatedAt());
		assertEquals(1779889000L, userToSave.getUpdatedAt());

		assertEquals(1L, response.id());
		assertEquals("user@example.com", response.email());
		assertEquals("nickname", response.nickname());
		assertEquals(UserStatus.ACTIVE, response.status());
		assertEquals(1779889000L, response.createdAt());
	}

	@Test
	void joinRejectsDuplicateActiveEmail() {
		JoinRequest request = new JoinRequest("user@example.com", "plainPassword", "nickname");

		when(userRepository.existsActiveByEmail("user@example.com")).thenReturn(true);

		DuplicateActiveEmailException exception = assertThrows(
			DuplicateActiveEmailException.class,
			() -> userJoinService.join(request)
		);

		assertTrue(exception.getMessage().contains("user@example.com"));
		verify(passwordEncoder, never()).encode("plainPassword");
		verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
	}
}
