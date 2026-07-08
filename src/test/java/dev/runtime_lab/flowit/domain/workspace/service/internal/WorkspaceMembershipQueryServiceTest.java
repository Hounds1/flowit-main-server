package dev.runtime_lab.flowit.domain.workspace.service.internal;

import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMemberRole;
import dev.runtime_lab.flowit.domain.workspace.repository.projection.WorkspaceMemberProfileProjection;
import dev.runtime_lab.flowit.domain.workspace.repository.projection.WorkspaceMembershipProjection;
import dev.runtime_lab.flowit.domain.workspace.repository.WorkspaceMemberRepository;
import dev.runtime_lab.flowit.domain.workspace.service.internal.contract.WorkspaceMembershipSummary;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkspaceMembershipQueryServiceTest {

	private final WorkspaceMemberRepository workspaceMemberRepository = mock(WorkspaceMemberRepository.class);
	private final WorkspaceMembershipQueryService workspaceMembershipQueryService = new WorkspaceMembershipQueryService(
		workspaceMemberRepository
	);

	@Test
	void findActiveMembershipSummariesConvertsRepositoryProjections() {
		List<WorkspaceMembershipProjection> projections = List.of(
			new WorkspaceMembershipProjection(10L, "Flowit", "Team workspace", 3L, WorkspaceMemberRole.OWNER, 2L)
		);

		when(workspaceMemberRepository.findActiveMembershipsByUserId(1L)).thenReturn(projections);

		List<WorkspaceMembershipSummary> response = workspaceMembershipQueryService.findActiveMembershipSummaries(1L);

		assertEquals(1, response.size());
		assertEquals(10L, response.get(0).workspaceId());
		assertEquals("Flowit", response.get(0).workspaceName());
		assertEquals(WorkspaceMemberRole.OWNER, response.get(0).role());
		verify(workspaceMemberRepository).findActiveMembershipsByUserId(1L);
	}

	@Test
	void findActiveMemberUserIdsDelegatesRepository() {
		when(workspaceMemberRepository.findActiveUserIdsByWorkspaceId(10L)).thenReturn(List.of(1L, 2L));

		List<Long> userIds = workspaceMembershipQueryService.findActiveMemberUserIds(10L);

		assertEquals(List.of(1L, 2L), userIds);
		verify(workspaceMemberRepository).findActiveUserIdsByWorkspaceId(10L);
	}

	@Test
	void findMemberUserIdDelegatesRepository() {
		when(workspaceMemberRepository.findUserIdByWorkspaceIdAndMemberId(10L, 100L))
			.thenReturn(Optional.of(1L));

		Optional<Long> userId = workspaceMembershipQueryService.findMemberUserId(10L, 100L);

		assertTrue(userId.isPresent());
		assertEquals(1L, userId.get());
		verify(workspaceMemberRepository).findUserIdByWorkspaceIdAndMemberId(10L, 100L);
	}

	@Test
	void findActiveProfileImageUrlByWorkspaceIdAndMemberIdConvertsRepositoryProjection() {
		when(workspaceMemberRepository.findActiveProfileByWorkspaceIdAndMemberId(10L, 100L))
			.thenReturn(Optional.of(new WorkspaceMemberProfileProjection(100L, 1L, "User", 300L)));

		Optional<String> profileImageUrl =
			workspaceMembershipQueryService.findActiveProfileImageUrlByWorkspaceIdAndMemberId(10L, 100L);

		assertTrue(profileImageUrl.isPresent());
		assertEquals("/v1/workspaces/10/members/100/profile-image", profileImageUrl.get());
		verify(workspaceMemberRepository).findActiveProfileByWorkspaceIdAndMemberId(10L, 100L);
	}

	@Test
	void findActiveProfileImageUrlByWorkspaceIdAndUserIdConvertsRepositoryProjection() {
		when(workspaceMemberRepository.findActiveProfileByWorkspaceIdAndUserId(10L, 1L))
			.thenReturn(Optional.of(new WorkspaceMemberProfileProjection(100L, 1L, "User", 300L)));

		Optional<String> profileImageUrl =
			workspaceMembershipQueryService.findActiveProfileImageUrlByWorkspaceIdAndUserId(10L, 1L);

		assertTrue(profileImageUrl.isPresent());
		assertEquals("/v1/workspaces/10/members/100/profile-image", profileImageUrl.get());
		verify(workspaceMemberRepository).findActiveProfileByWorkspaceIdAndUserId(10L, 1L);
	}

	@Test
	void findActiveProfileImageUrlReturnsEmptyWhenProfileImageDoesNotExist() {
		when(workspaceMemberRepository.findActiveProfileByWorkspaceIdAndMemberId(10L, 100L))
			.thenReturn(Optional.of(new WorkspaceMemberProfileProjection(100L, 1L, "User", null)));

		Optional<String> profileImageUrl =
			workspaceMembershipQueryService.findActiveProfileImageUrlByWorkspaceIdAndMemberId(10L, 100L);

		assertTrue(profileImageUrl.isEmpty());
	}
}
