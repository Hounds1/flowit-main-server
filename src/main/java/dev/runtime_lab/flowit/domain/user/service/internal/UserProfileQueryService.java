package dev.runtime_lab.flowit.domain.user.service.internal;

import dev.runtime_lab.flowit.domain.user.dto.UserMeResponse;
import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@InternalService
@RequiredArgsConstructor
public class UserProfileQueryService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public Optional<String> findCurrentUserProfileImageUrl(Long userId) {
		return userRepository.findActiveProfileById(userId)
			.map(profile -> UserMeResponse.profileImageUrl(profile.profileImageFileId()));
	}
}
