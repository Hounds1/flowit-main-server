package dev.runtime_lab.flowit.domain.user.service.internal;

import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import dev.runtime_lab.flowit.domain.user.repository.projection.UserProfileProjection;
import dev.runtime_lab.flowit.domain.user.service.internal.contract.UserProfileSummary;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileQueryServiceTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final UserProfileQueryService userProfileQueryService = new UserProfileQueryService(userRepository);

	@Test
	void findCurrentUserProfileReturnsDisplayNameAndProfileImageUrl() {
		when(userRepository.findActiveProfileById(1L))
			.thenReturn(Optional.of(new UserProfileProjection(1L, "User", 300L)));

		Optional<UserProfileSummary> profile = userProfileQueryService.findCurrentUserProfile(1L);

		assertTrue(profile.isPresent());
		assertEquals("User", profile.get().displayName());
		assertEquals("/v1/users/me/profile-image", profile.get().profileImageUrl());
		verify(userRepository).findActiveProfileById(1L);
	}

	@Test
	void findCurrentUserProfileImageUrlReturnsCurrentUserProfileImageUrl() {
		when(userRepository.findActiveProfileById(1L))
			.thenReturn(Optional.of(new UserProfileProjection(1L, "User", 300L)));

		Optional<String> profileImageUrl = userProfileQueryService.findCurrentUserProfileImageUrl(1L);

		assertTrue(profileImageUrl.isPresent());
		assertEquals("/v1/users/me/profile-image", profileImageUrl.get());
		verify(userRepository).findActiveProfileById(1L);
	}

	@Test
	void findCurrentUserProfileImageUrlReturnsEmptyWhenProfileImageDoesNotExist() {
		when(userRepository.findActiveProfileById(1L))
			.thenReturn(Optional.of(new UserProfileProjection(1L, "User", null)));

		Optional<String> profileImageUrl = userProfileQueryService.findCurrentUserProfileImageUrl(1L);

		assertTrue(profileImageUrl.isEmpty());
	}
}
