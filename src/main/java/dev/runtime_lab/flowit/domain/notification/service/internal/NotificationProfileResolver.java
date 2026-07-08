package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileSourceType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.user.repository.UserRepository;
import dev.runtime_lab.flowit.domain.user.repository.projection.UserProfileProjection;
import dev.runtime_lab.flowit.domain.workspace.dto.WorkspaceMemberResponse;
import dev.runtime_lab.flowit.domain.workspace.repository.WorkspaceMemberRepository;
import dev.runtime_lab.flowit.domain.workspace.repository.projection.WorkspaceMemberProfileProjection;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import lombok.RequiredArgsConstructor;

@InternalService
@RequiredArgsConstructor
public class NotificationProfileResolver {

	private static final String CURRENT_USER_PROFILE_IMAGE_URL = "/v1/users/me/profile-image";

	private final UserRepository userRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;

	public NotificationProfileResponse resolve(NotificationAlert notificationAlert, Long recipientUserId) {
		return switch (notificationAlert.getType()) {
			case TASK_ASSIGNED, TASK_UNASSIGNED, WORKSPACE_ACCESS_REVOKED ->
				recipientProfile(recipientUserId);
			case WORKSPACE_MEMBER_JOINED,
				WORKSPACE_MEMBER_ROLE_CHANGED,
				WORKSPACE_MEMBER_REMOVED,
				WORKSPACE_MEMBER_WITHDRAWN -> subjectWorkspaceMemberProfile(notificationAlert);
			case TASK_CREATED,
				TASK_DATE_CHANGED,
				TASK_STATUS_CHANGED,
				TASK_PROGRESS_CHANGED -> actorWorkspaceMemberProfile(notificationAlert);
		};
	}

	private NotificationProfileResponse recipientProfile(Long recipientUserId) {
		if (recipientUserId == null) {
			return new NotificationProfileResponse(NotificationProfileSourceType.RECIPIENT, null);
		}

		return userRepository.findActiveProfileById(recipientUserId)
			.map(profile -> new NotificationProfileResponse(
				NotificationProfileSourceType.RECIPIENT,
				currentUserProfileImageUrl(profile)
			))
			.orElseGet(() -> new NotificationProfileResponse(NotificationProfileSourceType.RECIPIENT, null));
	}

	private String currentUserProfileImageUrl(UserProfileProjection profile) {
		return profile.profileImageFileId() == null ? null : CURRENT_USER_PROFILE_IMAGE_URL;
	}

	private NotificationProfileResponse subjectWorkspaceMemberProfile(NotificationAlert notificationAlert) {
		if (notificationAlert.getScopeType() != NotificationScopeType.WORKSPACE
			|| notificationAlert.getSubjectType() != NotificationSubjectType.WORKSPACE_MEMBER
			|| notificationAlert.getSubjectId() == null) {
			return new NotificationProfileResponse(NotificationProfileSourceType.SUBJECT, null);
		}

		return workspaceMemberRepository
			.findActiveProfileByWorkspaceIdAndMemberId(
				notificationAlert.getScopeId(),
				notificationAlert.getSubjectId()
			)
			.map(profile -> workspaceMemberProfile(
				notificationAlert.getScopeId(),
				profile,
				NotificationProfileSourceType.SUBJECT
			))
			.orElseGet(() -> new NotificationProfileResponse(NotificationProfileSourceType.SUBJECT, null));
	}

	private NotificationProfileResponse actorWorkspaceMemberProfile(NotificationAlert notificationAlert) {
		if (notificationAlert.getScopeType() != NotificationScopeType.WORKSPACE
			|| notificationAlert.getActorType() != NotificationActorType.USER
			|| notificationAlert.getActorId() == null) {
			return new NotificationProfileResponse(NotificationProfileSourceType.ACTOR, null);
		}

		return workspaceMemberRepository
			.findActiveProfileByWorkspaceIdAndUserId(
				notificationAlert.getScopeId(),
				notificationAlert.getActorId()
			)
			.map(profile -> workspaceMemberProfile(
				notificationAlert.getScopeId(),
				profile,
				NotificationProfileSourceType.ACTOR
			))
			.orElseGet(() -> new NotificationProfileResponse(NotificationProfileSourceType.ACTOR, null));
	}

	private NotificationProfileResponse workspaceMemberProfile(
		Long workspaceId,
		WorkspaceMemberProfileProjection profile,
		NotificationProfileSourceType source
	) {
		return new NotificationProfileResponse(
			source,
			WorkspaceMemberResponse.profileImageUrl(workspaceId, profile.memberId(), profile.profileImageFileId())
		);
	}
}
