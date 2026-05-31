package dev.runtime_lab.flowit.domain.workspace.service;

import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.user.entity.UserStatus;
import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import dev.runtime_lab.flowit.domain.workspace.entity.Workspace;
import dev.runtime_lab.flowit.domain.workspace.exception.WorkspaceAccessDeniedException;
import dev.runtime_lab.flowit.domain.workspace.exception.WorkspaceNotFoundException;
import dev.runtime_lab.flowit.domain.workspace.repository.WorkspaceMemberRepository;
import dev.runtime_lab.flowit.domain.workspace.repository.WorkspaceRepository;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import dev.runtime_lab.flowit.global.security.authentication.InvalidAuthenticatedUserException;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceDeleteService {

	private final UserRepository userRepository;
	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final Clock clock;

	@Transactional
	public void delete(CurrentUser currentUser, Long workspaceId) {
		User user = userRepository.findActiveById(currentUser.id())
			.filter(foundUser -> foundUser.getStatus() == UserStatus.ACTIVE)
			.orElseThrow(InvalidAuthenticatedUserException::new);

		Workspace workspace = workspaceRepository.findActiveByIdForUpdate(workspaceId)
			.orElseThrow(WorkspaceNotFoundException::new);

		if (!workspaceMemberRepository.existsActiveOwnerByWorkspaceAndUser(workspace, user)) {
			throw new WorkspaceAccessDeniedException();
		}

		Long deletedAt = Instant.now(clock).getEpochSecond();
		workspace.softDelete(deletedAt);
		workspaceMemberRepository.softDeleteActiveByWorkspaceId(workspace.getId(), deletedAt);
	}
}
