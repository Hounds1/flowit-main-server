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
import dev.runtime_lab.flowit.domain.user.service.internal.CurrentUserProvider;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserProfileService {

	private final CurrentUserProvider currentUserProvider;
	private final ProfileImageFileService profileImageFileService;
	private final Clock clock;

	@Transactional
	public UserUpdateResponse update(CurrentUser currentUser, UserUpdateRequest request) {
		User user = currentUserProvider.findActiveForUpdate(currentUser);

		Long updatedAt = Instant.now(clock).getEpochSecond();
		if (request.nickname() != null) {
			user.changeNickname(request.nickname(), updatedAt);
		}

		return UserUpdateResponse.from(user);
	}

	@Transactional
	public UserProfileImageUpdateResponse replaceProfileImage(CurrentUser currentUser, MultipartFile imageFile) {
		User user = currentUserProvider.findActiveForUpdate(currentUser);

		Instant now = Instant.now(clock);
		FileMetadata newFileMetadata = profileImageFileService.store(user.getId(), imageFile);
		FileMetadata oldFileMetadata = user.replaceProfileImageFile(newFileMetadata, now.getEpochSecond());

		if (oldFileMetadata != null) {
			profileImageFileService.deleteAfterCommit(oldFileMetadata);
		}

		return UserProfileImageUpdateResponse.from(newFileMetadata);
	}

	@Transactional(readOnly = true)
	public UserProfileImageContentResponse getProfileImage(CurrentUser currentUser) {
		User user = currentUserProvider.findActive(currentUser);

		FileMetadata profileImageFile = user.getProfileImageFile();
		if (profileImageFile == null) {
			throw new ProfileImageNotFoundException();
		}

		ProfileImageFileContent content = profileImageFileService.load(profileImageFile);

		return new UserProfileImageContentResponse(profileImageFile.getContentType(), content.bytes());
	}
}
