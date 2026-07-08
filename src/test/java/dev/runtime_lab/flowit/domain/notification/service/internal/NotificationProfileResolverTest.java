package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileSourceType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import dev.runtime_lab.flowit.domain.user.repository.projection.UserProfileProjection;
import dev.runtime_lab.flowit.domain.workspace.repository.WorkspaceMemberRepository;
import dev.runtime_lab.flowit.domain.workspace.repository.projection.WorkspaceMemberProfileProjection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationProfileResolverTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final WorkspaceMemberRepository workspaceMemberRepository = mock(WorkspaceMemberRepository.class);
	private final NotificationProfileResolver resolver =
		new NotificationProfileResolver(userRepository, workspaceMemberRepository);

	@Test
	void resolvesActorProfileForTaskChangeAlerts() {
		when(workspaceMemberRepository.findActiveProfileByWorkspaceIdAndUserId(12L, 34L))
			.thenReturn(Optional.of(new WorkspaceMemberProfileProjection(301L, 34L, "Actor", 3001L)));

		List<NotificationAlertType> actorProfileTypes = List.of(
			NotificationAlertType.TASK_CREATED,
			NotificationAlertType.TASK_DATE_CHANGED,
			NotificationAlertType.TASK_STATUS_CHANGED,
			NotificationAlertType.TASK_PROGRESS_CHANGED
		);

		for (NotificationAlertType type : actorProfileTypes) {
			NotificationProfileResponse profile = resolver.resolve(alert(type), 36L);

			assertEquals(NotificationProfileSourceType.ACTOR, profile.source());
			assertEquals("/v1/workspaces/12/members/301/profile-image", profile.profileImageUrl());
		}
	}

	@Test
	void resolvesRecipientProfileForDirectRecipientAlerts() {
		when(userRepository.findActiveProfileById(36L))
			.thenReturn(Optional.of(new UserProfileProjection(36L, "Recipient", 3002L)));

		List<NotificationAlertType> recipientProfileTypes = List.of(
			NotificationAlertType.TASK_ASSIGNED,
			NotificationAlertType.TASK_UNASSIGNED
		);

		for (NotificationAlertType type : recipientProfileTypes) {
			NotificationProfileResponse profile = resolver.resolve(alert(type), 36L);

			assertEquals(NotificationProfileSourceType.RECIPIENT, profile.source());
			assertEquals("/v1/users/me/profile-image", profile.profileImageUrl());
		}
	}

	@Test
	void resolvesCurrentUserProfileForWorkspaceAccessRevoked() {
		when(userRepository.findActiveProfileById(36L))
			.thenReturn(Optional.of(new UserProfileProjection(36L, "Current Name", 3002L)));

		NotificationProfileResponse profile = resolver.resolve(
			alert(NotificationAlertType.WORKSPACE_ACCESS_REVOKED),
			36L
		);

		assertEquals(NotificationProfileSourceType.RECIPIENT, profile.source());
		assertEquals("/v1/users/me/profile-image", profile.profileImageUrl());
	}

	@Test
	void resolvesSubjectProfileForActiveWorkspaceMemberAlerts() {
		when(workspaceMemberRepository.findActiveProfileByWorkspaceIdAndMemberId(12L, 55L))
			.thenReturn(Optional.of(new WorkspaceMemberProfileProjection(55L, 36L, "Target", 3003L)));

		List<NotificationAlertType> subjectProfileTypes = List.of(
			NotificationAlertType.WORKSPACE_MEMBER_JOINED,
			NotificationAlertType.WORKSPACE_MEMBER_ROLE_CHANGED
		);

		for (NotificationAlertType type : subjectProfileTypes) {
			NotificationProfileResponse profile = resolver.resolve(alert(type), 34L);

			assertEquals(NotificationProfileSourceType.SUBJECT, profile.source());
			assertEquals("/v1/workspaces/12/members/55/profile-image", profile.profileImageUrl());
		}
	}

	@Test
	void returnsNullImageWhenSubjectMemberIsNotActive() {
		when(workspaceMemberRepository.findActiveProfileByWorkspaceIdAndMemberId(12L, 55L))
			.thenReturn(Optional.empty());

		List<NotificationAlertType> inactiveSubjectTypes = List.of(
			NotificationAlertType.WORKSPACE_MEMBER_REMOVED,
			NotificationAlertType.WORKSPACE_MEMBER_WITHDRAWN
		);

		for (NotificationAlertType type : inactiveSubjectTypes) {
			NotificationProfileResponse profile = resolver.resolve(alert(type), 34L);

			assertEquals(NotificationProfileSourceType.SUBJECT, profile.source());
			assertNull(profile.profileImageUrl());
		}
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
