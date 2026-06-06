package dev.runtime_lab.flowit.domain.workspace.service.internal;

import dev.runtime_lab.flowit.domain.user.dto.UserMeWorkspaceResponse;
import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMemberRole;
import dev.runtime_lab.flowit.domain.workspace.repository.WorkspaceMemberRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkspaceMembershipQueryServiceTest {

	private final WorkspaceMemberRepository workspaceMemberRepository = mock(WorkspaceMemberRepository.class);
	private final WorkspaceMembershipQueryService workspaceMembershipQueryService = new WorkspaceMembershipQueryService(
		workspaceMemberRepository
	);

	@Test
	void findActiveUserWorkspacesDelegatesToRepository() {
		List<UserMeWorkspaceResponse> expected = List.of(
			new UserMeWorkspaceResponse(10L, "Flowit", "Team workspace", 3L, WorkspaceMemberRole.OWNER, 2L)
		);

		when(workspaceMemberRepository.findActiveUserWorkspaces(1L)).thenReturn(expected);

		List<UserMeWorkspaceResponse> response = workspaceMembershipQueryService.findActiveUserWorkspaces(1L);

		assertSame(expected, response);
		verify(workspaceMemberRepository).findActiveUserWorkspaces(1L);
	}
}
