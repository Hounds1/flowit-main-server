package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileSourceType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.user.service.internal.UserProfileQueryService;
import dev.runtime_lab.flowit.domain.user.service.internal.contract.UserProfileSummary;
import dev.runtime_lab.flowit.domain.workspace.service.internal.WorkspaceMembershipQueryService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationProfileResolverTest {

	private final UserProfileQueryService userProfileQueryService = mock(UserProfileQueryService.class);
	private final WorkspaceMembershipQueryService workspaceMembershipQueryService =
		mock(WorkspaceMembershipQueryService.class);
	private final NotificationProfileResolver resolver =
		new NotificationProfileResolver(userProfileQueryService, workspaceMembershipQueryService);

	@Test
	void resolvesActorProfileForActorFocusedAlerts() {
		when(workspaceMembershipQueryService.findActiveProfileImageUrlByWorkspaceIdAndUserId(12L, 34L))
			.thenReturn(Optional.of("/v1/workspaces/12/members/301/profile-image"));

		List<NotificationAlertType> actorProfileTypes = List.of(
			NotificationAlertType.TASK_CREATED,
			NotificationAlertType.TASK_DATE_CHANGED,
			NotificationAlertType.TASK_STATUS_CHANGED,
			NotificationAlertType.TASK_PROGRESS_CHANGED,
			NotificationAlertType.WORKSPACE_MEMBER_REMOVED
		);

		for (NotificationAlertType type : actorProfileTypes) {
			NotificationProfileResponse profile = resolver.resolve(alert(type), 36L);

			assertEquals(NotificationProfileSourceType.ACTOR, profile.source());
			assertEquals("Actor Snapshot", profile.displayName());
			assertEquals("/v1/workspaces/12/members/301/profile-image", profile.profileImageUrl());
		}
	}

	@Test
	void resolvesRecipientProfileForDirectRecipientAlerts() {
		when(userProfileQueryService.findCurrentUserProfile(36L))
			.thenReturn(Optional.of(new UserProfileSummary("Recipient", "/v1/users/me/profile-image")));

		List<NotificationAlertType> recipientProfileTypes = List.of(
			NotificationAlertType.TASK_ASSIGNED,
			NotificationAlertType.TASK_UNASSIGNED
		);

		for (NotificationAlertType type : recipientProfileTypes) {
			NotificationProfileResponse profile = resolver.resolve(alert(type), 36L);

			assertEquals(NotificationProfileSourceType.RECIPIENT, profile.source());
			assertEquals("Recipient", profile.displayName());
			assertEquals("/v1/users/me/profile-image", profile.profileImageUrl());
		}
	}

	@Test
	void resolvesCurrentUserProfileForWorkspaceAccessRevoked() {
		when(userProfileQueryService.findCurrentUserProfile(36L))
			.thenReturn(Optional.of(new UserProfileSummary("Recipient", "/v1/users/me/profile-image")));

		NotificationProfileResponse profile = resolver.resolve(
			alert(NotificationAlertType.WORKSPACE_ACCESS_REVOKED),
			36L
		);

		assertEquals(NotificationProfileSourceType.RECIPIENT, profile.source());
		assertEquals("Recipient", profile.displayName());
		assertEquals("/v1/users/me/profile-image", profile.profileImageUrl());
	}

	@Test
	void resolvesSubjectProfileForActiveWorkspaceMemberAlerts() {
		when(workspaceMembershipQueryService.findActiveProfileImageUrlByWorkspaceIdAndMemberId(12L, 55L))
			.thenReturn(Optional.of("/v1/workspaces/12/members/55/profile-image"));

		List<NotificationAlertType> subjectProfileTypes = List.of(
			NotificationAlertType.WORKSPACE_MEMBER_JOINED,
			NotificationAlertType.WORKSPACE_MEMBER_ROLE_CHANGED
		);

		for (NotificationAlertType type : subjectProfileTypes) {
			NotificationProfileResponse profile = resolver.resolve(alert(type), 34L);

			assertEquals(NotificationProfileSourceType.SUBJECT, profile.source());
			assertEquals("Target Snapshot", profile.displayName());
			assertEquals("/v1/workspaces/12/members/55/profile-image", profile.profileImageUrl());
		}
	}

	@Test
	void returnsNullImageWhenWithdrawnSubjectMemberIsNotActive() {
		when(workspaceMembershipQueryService.findActiveProfileImageUrlByWorkspaceIdAndMemberId(12L, 55L))
			.thenReturn(Optional.empty());

		NotificationProfileResponse profile = resolver.resolve(
			alert(NotificationAlertType.WORKSPACE_MEMBER_WITHDRAWN),
			34L
		);

		assertEquals(NotificationProfileSourceType.SUBJECT, profile.source());
		assertEquals("Target Snapshot", profile.displayName());
		assertNull(profile.profileImageUrl());
	}

	private NotificationAlert alert(NotificationAlertType type) {
		return NotificationAlert.builder()
			.type(type)
			.scopeType(NotificationScopeType.WORKSPACE)
			.scopeId(12L)
			.actorType(NotificationActorType.USER)
			.actorId(34L)
			.actorNameSnapshot("Actor Snapshot")
			.subjectType(NotificationSubjectType.WORKSPACE_MEMBER)
			.subjectId(55L)
			.subjectNameSnapshot("Target Snapshot")
			.build();
	}
}
