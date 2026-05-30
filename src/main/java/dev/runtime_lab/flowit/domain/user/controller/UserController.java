package dev.runtime_lab.flowit.domain.user.controller;

import dev.runtime_lab.flowit.domain.user.dto.UserMeResponse;
import dev.runtime_lab.flowit.domain.user.dto.UserProfileImageUpdateResponse;
import dev.runtime_lab.flowit.domain.user.service.UserMeService;
import dev.runtime_lab.flowit.domain.user.service.UserProfileImageUpdateService;
import dev.runtime_lab.flowit.global.security.authentication.AuthenticatedUser;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserMeService userMeService;
	private final UserProfileImageUpdateService userProfileImageUpdateService;

	@GetMapping("/me")
	public UserMeResponse me(@AuthenticatedUser CurrentUser currentUser) {
		return userMeService.getMe(currentUser);
	}

	@PutMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public UserProfileImageUpdateResponse replaceProfileImage(
		@AuthenticatedUser CurrentUser currentUser,
		@RequestPart("file") MultipartFile file
	) {
		return userProfileImageUpdateService.replace(currentUser, file);
	}
}
