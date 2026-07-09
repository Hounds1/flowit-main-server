package dev.runtime_lab.flowit.domain.user.service.internal;

import dev.runtime_lab.flowit.domain.user.dto.UserMeResponse;
import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import dev.runtime_lab.flowit.domain.user.service.internal.contract.UserProfileSummary;
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
		return findCurrentUserProfile(userId)
			.map(UserProfileSummary::profileImageUrl)
			.filter(profileImageUrl -> profileImageUrl != null);
	}

	@Transactional(readOnly = true)
	public Optional<UserProfileSummary> findCurrentUserProfile(Long userId) {
		return userRepository.findActiveProfileById(userId)
			.map(profile -> new UserProfileSummary(
				profile.name(),
				UserMeResponse.profileImageUrl(profile.profileImageFileId())
			));
	}
}
