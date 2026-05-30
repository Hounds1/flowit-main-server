package dev.runtime_lab.flowit.domain.user.service;

import dev.runtime_lab.flowit.domain.file.entity.FileMetadata;
import dev.runtime_lab.flowit.domain.file.repository.FileMetadataRepository;
import dev.runtime_lab.flowit.domain.file.storage.LocalProfileImageStorage;
import dev.runtime_lab.flowit.domain.file.storage.StoredProfileImageFile;
import dev.runtime_lab.flowit.domain.user.dto.UserProfileImageUpdateResponse;
import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.user.entity.UserStatus;
import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import dev.runtime_lab.flowit.global.security.authentication.InvalidAuthenticatedUserException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileImageUpdateServiceTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final FileMetadataRepository fileMetadataRepository = mock(FileMetadataRepository.class);
	private final LocalProfileImageStorage localProfileImageStorage = mock(LocalProfileImageStorage.class);
	private final Clock clock = Clock.fixed(Instant.ofEpochSecond(1779889000L), ZoneOffset.UTC);
	private final UserProfileImageUpdateService service = new UserProfileImageUpdateService(
		userRepository,
		fileMetadataRepository,
		localProfileImageStorage,
		clock
	);

	@Test
	void replaceCreatesNewFileMetadataAndDeletesPreviousRow() {
		MultipartFile multipartFile = mock(MultipartFile.class);
		FileMetadata oldFileMetadata = fileMetadata(2001L, "users/1/old.png");
		FileMetadata savedFileMetadata = fileMetadata(3001L, "users/1/new.png");
		User user = activeUser(oldFileMetadata);
		ArgumentCaptor<FileMetadata> fileMetadataCaptor = ArgumentCaptor.forClass(FileMetadata.class);

		when(userRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(user));
		when(localProfileImageStorage.store(1L, multipartFile))
			.thenReturn(new StoredProfileImageFile("users/1/new.png", "avatar.png", "image/png", 68L, 1, 1));
		when(fileMetadataRepository.save(fileMetadataCaptor.capture())).thenReturn(savedFileMetadata);

		UserProfileImageUpdateResponse response = service.replace(
			new CurrentUser(1L, "user@example.com", "nickname"),
			multipartFile
		);

		FileMetadata fileMetadataToSave = fileMetadataCaptor.getValue();
		assertEquals("users/1/new.png", fileMetadataToSave.getStorageKey());
		assertEquals("avatar.png", fileMetadataToSave.getOriginalFilename());
		assertEquals("image/png", fileMetadataToSave.getContentType());
		assertEquals(68L, fileMetadataToSave.getSizeBytes());
		assertEquals(1, fileMetadataToSave.getWidth());
		assertEquals(1, fileMetadataToSave.getHeight());
		assertEquals(1779889000000L, fileMetadataToSave.getCreatedAt());
		assertEquals(1779889000000L, fileMetadataToSave.getUpdatedAt());
		assertSame(savedFileMetadata, user.getProfileImageFile());
		assertEquals(1779889000L, user.getUpdatedAt());
		assertEquals(3001L, response.fileId());

		verify(fileMetadataRepository).delete(oldFileMetadata);
		verify(localProfileImageStorage).deleteIfExists("users/1/old.png");
	}

	@Test
	void replaceRejectsMissingUserBeforeWritingFile() {
		MultipartFile multipartFile = mock(MultipartFile.class);
		when(userRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.empty());

		assertThrows(
			InvalidAuthenticatedUserException.class,
			() -> service.replace(new CurrentUser(1L, "user@example.com", "nickname"), multipartFile)
		);

		verify(localProfileImageStorage, never()).store(any(), any());
		verify(fileMetadataRepository, never()).save(any());
	}

	private User activeUser(FileMetadata profileImageFile) {
		return User.builder()
			.id(1L)
			.email("user@example.com")
			.passwordHash("hash")
			.name("nickname")
			.profileImageFile(profileImageFile)
			.status(UserStatus.ACTIVE)
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
