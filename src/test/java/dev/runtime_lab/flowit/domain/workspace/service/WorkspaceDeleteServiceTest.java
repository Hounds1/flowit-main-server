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
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkspaceDeleteServiceTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final WorkspaceRepository workspaceRepository = mock(WorkspaceRepository.class);
	private final WorkspaceMemberRepository workspaceMemberRepository = mock(WorkspaceMemberRepository.class);
	private final Clock clock = Clock.fixed(Instant.ofEpochSecond(1779889000L), ZoneOffset.UTC);
	private final WorkspaceDeleteService workspaceDeleteService = new WorkspaceDeleteService(
		userRepository,
		workspaceRepository,
		workspaceMemberRepository,
		clock
	);

	@Test
	void deleteSoftDeletesWorkspaceAndActiveMembersWhenCurrentUserIsOwner() {
		CurrentUser currentUser = new CurrentUser(1L, "user@example.com", "nickname");
		User user = activeUser();
		Workspace workspace = workspace();

		when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));
		when(workspaceRepository.findActiveByIdForUpdate(10L)).thenReturn(Optional.of(workspace));
		when(workspaceMemberRepository.existsActiveOwnerByWorkspaceAndUser(workspace, user)).thenReturn(true);

		workspaceDeleteService.delete(currentUser, 10L);

		assertEquals(1779889000L, workspace.getDeletedAt());
		assertEquals(1779889000L, workspace.getUpdatedAt());
		verify(workspaceMemberRepository).softDeleteActiveByWorkspaceId(10L, 1779889000L);
	}

	@Test
	void deleteRejectsMissingUser() {
		CurrentUser currentUser = new CurrentUser(1L, "user@example.com", "nickname");

		when(userRepository.findActiveById(1L)).thenReturn(Optional.empty());

		assertThrows(InvalidAuthenticatedUserException.class, () -> workspaceDeleteService.delete(currentUser, 10L));
		verify(workspaceRepository, never()).findActiveByIdForUpdate(10L);
		verify(workspaceMemberRepository, never()).softDeleteActiveByWorkspaceId(10L, 1779889000L);
	}

	@Test
	void deleteRejectsInactiveUser() {
		CurrentUser currentUser = new CurrentUser(1L, "user@example.com", "nickname");
		User user = User.builder()
			.id(1L)
			.email("user@example.com")
			.passwordHash("hash")
			.name("nickname")
			.status(UserStatus.LOCKED)
			.createdAt(1L)
			.updatedAt(1L)
			.build();

		when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));

		assertThrows(InvalidAuthenticatedUserException.class, () -> workspaceDeleteService.delete(currentUser, 10L));
		verify(workspaceRepository, never()).findActiveByIdForUpdate(10L);
		verify(workspaceMemberRepository, never()).softDeleteActiveByWorkspaceId(10L, 1779889000L);
	}

	@Test
	void deleteRejectsMissingWorkspace() {
		CurrentUser currentUser = new CurrentUser(1L, "user@example.com", "nickname");

		when(userRepository.findActiveById(1L)).thenReturn(Optional.of(activeUser()));
		when(workspaceRepository.findActiveByIdForUpdate(10L)).thenReturn(Optional.empty());

		assertThrows(WorkspaceNotFoundException.class, () -> workspaceDeleteService.delete(currentUser, 10L));
		verify(workspaceMemberRepository, never()).softDeleteActiveByWorkspaceId(10L, 1779889000L);
	}

	@Test
	void deleteRejectsNonOwnerMember() {
		CurrentUser currentUser = new CurrentUser(1L, "user@example.com", "nickname");
		User user = activeUser();
		Workspace workspace = workspace();

		when(userRepository.findActiveById(1L)).thenReturn(Optional.of(user));
		when(workspaceRepository.findActiveByIdForUpdate(10L)).thenReturn(Optional.of(workspace));
		when(workspaceMemberRepository.existsActiveOwnerByWorkspaceAndUser(workspace, user)).thenReturn(false);

		assertThrows(WorkspaceAccessDeniedException.class, () -> workspaceDeleteService.delete(currentUser, 10L));
		assertNull(workspace.getDeletedAt());
		verify(workspaceMemberRepository, never()).softDeleteActiveByWorkspaceId(10L, 1779889000L);
	}

	private User activeUser() {
		return User.builder()
			.id(1L)
			.email("user@example.com")
			.passwordHash("hash")
			.name("nickname")
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}

	private Workspace workspace() {
		return Workspace.builder()
			.id(10L)
			.name("Flowit")
			.inviteCode("A1B2-C3D4-E5F6")
			.createdBy(activeUser())
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}
}
