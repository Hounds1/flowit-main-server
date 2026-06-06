package dev.runtime_lab.flowit.domain.user.service;

import dev.runtime_lab.flowit.domain.file.entity.FileMetadata;
import dev.runtime_lab.flowit.domain.file.exception.ProfileImageNotFoundException;
import dev.runtime_lab.flowit.domain.file.service.internal.ProfileImageFileService;
import dev.runtime_lab.flowit.domain.file.storage.ProfileImageFileContent;
import dev.runtime_lab.flowit.domain.user.dto.UserProfileImageContentResponse;
import dev.runtime_lab.flowit.domain.user.dto.UserProfileImageUpdateResponse;
import dev.runtime_lab.flowit.domain.user.dto.UserUpdateRequest;
import dev.runtime_lab.flowit.domain.user.dto.UserUpdateResponse;
import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.user.entity.UserStatus;
import dev.runtime_lab.flowit.domain.user.service.internal.CurrentUserProvider;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import dev.runtime_lab.flowit.global.security.authentication.InvalidAuthenticatedUserException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserProfileServiceTest {

	private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
	private final ProfileImageFileService profileImageFileService = mock(ProfileImageFileService.class);
	private final Clock clock = Clock.fixed(Instant.parse("2026-05-30T12:00:00Z"), ZoneOffset.UTC);
	private final UserProfileService service = new UserProfileService(
		currentUserProvider,
		profileImageFileService,
		clock
	);

	@Test
	void updateChangesCurrentUserNickname() {
		User user = activeUser(null);
		when(currentUserProvider.findActiveForUpdate(any(CurrentUser.class))).thenReturn(user);

		UserUpdateResponse response = service.update(
			new CurrentUser(1L, "claim@example.com", "claim-name"),
			new UserUpdateRequest("new-nickname")
		);

		assertEquals("new-nickname", user.getName());
		assertEquals(1_780_142_400L, user.getUpdatedAt());
		assertEquals(1L, response.id());
		assertEquals("user@example.com", response.email());
		assertEquals("new-nickname", response.nickname());
		assertEquals(UserStatus.ACTIVE, response.status());
		assertEquals(1_780_142_400L, response.updatedAt());
		verify(currentUserProvider).findActiveForUpdate(any(CurrentUser.class));
	}

	@Test
	void updateRejectsMissingUser() {
		when(currentUserProvider.findActiveForUpdate(any(CurrentUser.class)))
			.thenThrow(new InvalidAuthenticatedUserException());

		assertThrows(
			InvalidAuthenticatedUserException.class,
			() -> service.update(
				new CurrentUser(1L, "user@example.com", "nickname"),
				new UserUpdateRequest("new-nickname")
			)
		);
		verify(currentUserProvider).findActiveForUpdate(any(CurrentUser.class));
	}

	@Test
	void updateRejectsInactiveUser() {
		when(currentUserProvider.findActiveForUpdate(any(CurrentUser.class)))
			.thenThrow(new InvalidAuthenticatedUserException());

		assertThrows(
			InvalidAuthenticatedUserException.class,
			() -> service.update(
				new CurrentUser(1L, "user@example.com", "nickname"),
				new UserUpdateRequest("new-nickname")
			)
		);
	}

	@Test
	void replaceProfileImageCreatesNewFileMetadataAndDeletesPreviousRow() {
		MultipartFile multipartFile = mock(MultipartFile.class);
		FileMetadata oldFileMetadata = fileMetadata(2001L, "users/1/old.png");
		FileMetadata savedFileMetadata = fileMetadata(3001L, "users/1/new.png");
		User user = activeUser(oldFileMetadata);

		when(currentUserProvider.findActiveForUpdate(any(CurrentUser.class))).thenReturn(user);
		when(profileImageFileService.store(1L, multipartFile)).thenReturn(savedFileMetadata);

		UserProfileImageUpdateResponse response = service.replaceProfileImage(
			new CurrentUser(1L, "user@example.com", "nickname"),
			multipartFile
		);

		assertSame(savedFileMetadata, user.getProfileImageFile());
		assertEquals(1_780_142_400L, user.getUpdatedAt());
		assertEquals(3001L, response.fileId());

		verify(profileImageFileService).store(1L, multipartFile);
		verify(profileImageFileService).deleteAfterCommit(oldFileMetadata);
	}

	@Test
	void replaceProfileImageRejectsMissingUserBeforeWritingFile() {
		MultipartFile multipartFile = mock(MultipartFile.class);
		when(currentUserProvider.findActiveForUpdate(any(CurrentUser.class)))
			.thenThrow(new InvalidAuthenticatedUserException());

		assertThrows(
			InvalidAuthenticatedUserException.class,
			() -> service.replaceProfileImage(new CurrentUser(1L, "user@example.com", "nickname"), multipartFile)
		);

		verify(profileImageFileService, never()).store(any(), any());
	}

	@Test
	void getProfileImageReturnsCurrentUserProfileImageContent() {
		byte[] bytes = new byte[] {1, 2, 3};
		FileMetadata profileImageFile = fileMetadata(3001L, "users/1/avatar.png");
		User user = activeUser(profileImageFile);

		when(currentUserProvider.findActive(any(CurrentUser.class))).thenReturn(user);
		when(profileImageFileService.load(profileImageFile)).thenReturn(new ProfileImageFileContent(bytes));

		UserProfileImageContentResponse response = service.getProfileImage(
			new CurrentUser(1L, "user@example.com", "nickname")
		);

		assertEquals("image/png", response.contentType());
		assertEquals(3L, response.contentLength());
		assertArrayEquals(bytes, response.bytes());
		verify(currentUserProvider).findActive(any(CurrentUser.class));
		verify(profileImageFileService).load(profileImageFile);
	}

	@Test
	void getProfileImageRejectsMissingUser() {
		when(currentUserProvider.findActive(any(CurrentUser.class))).thenThrow(new InvalidAuthenticatedUserException());

		assertThrows(
			InvalidAuthenticatedUserException.class,
			() -> service.getProfileImage(new CurrentUser(1L, "user@example.com", "nickname"))
		);
		verifyNoInteractions(profileImageFileService);
	}

	@Test
	void getProfileImageRejectsInactiveUser() {
		when(currentUserProvider.findActive(any(CurrentUser.class))).thenThrow(new InvalidAuthenticatedUserException());

		assertThrows(
			InvalidAuthenticatedUserException.class,
			() -> service.getProfileImage(new CurrentUser(1L, "user@example.com", "nickname"))
		);
		verifyNoInteractions(profileImageFileService);
	}

	@Test
	void getProfileImageRejectsUserWithoutProfileImage() {
		User user = activeUser(null);
		when(currentUserProvider.findActive(any(CurrentUser.class))).thenReturn(user);

		assertThrows(
			ProfileImageNotFoundException.class,
			() -> service.getProfileImage(new CurrentUser(1L, "user@example.com", "nickname"))
		);
		verifyNoInteractions(profileImageFileService);
	}

	private User activeUser(FileMetadata profileImageFile) {
		return activeUser(profileImageFile, UserStatus.ACTIVE);
	}

	private User activeUser(FileMetadata profileImageFile, UserStatus status) {
		return User.builder()
			.id(1L)
			.email("user@example.com")
			.passwordHash("hash")
			.name("nickname")
			.profileImageFile(profileImageFile)
			.status(status)
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}

	private FileMetadata fileMetadata(Long id, String storageKey) {
		return FileMetadata.builder()
			.id(id)
			.storageKey(storageKey)
			.originalFilename("avatar.png")
			.contentType("image/png")
			.sizeBytes(68L)
			.width(1)
			.height(1)
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}
}
