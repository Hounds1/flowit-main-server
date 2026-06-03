package dev.runtime_lab.flowit.domain.user.service;

import dev.runtime_lab.flowit.domain.user.dto.UserMeResponse;
import dev.runtime_lab.flowit.domain.user.dto.UserMeWorkspaceResponse;
import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import dev.runtime_lab.flowit.domain.user.service.internal.CurrentUserProvider;
import dev.runtime_lab.flowit.domain.workspace.repository.WorkspaceMemberRepository;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import dev.runtime_lab.flowit.global.security.authentication.InvalidAuthenticatedUserException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserMeService {

	private final UserRepository userRepository;
	private final CurrentUserProvider currentUserProvider;
	private final WorkspaceMemberRepository workspaceMemberRepository;

	@Transactional(readOnly = true)
	public UserMeResponse getMe(CurrentUser currentUser) {
		return userRepository.findActiveMeById(currentUser.id())
			.orElseThrow(InvalidAuthenticatedUserException::new);
	}

	@Transactional(readOnly = true)
	public List<UserMeWorkspaceResponse> getMeWorkspaces(CurrentUser currentUser) {
		currentUserProvider.findActive(currentUser);

		return workspaceMemberRepository.findActiveUserWorkspaces(currentUser.id());
	}
}
