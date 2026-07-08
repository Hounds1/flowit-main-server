package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileSourceType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.user.service.internal.UserProfileQueryService;
import dev.runtime_lab.flowit.domain.workspace.service.internal.WorkspaceMembershipQueryService;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import lombok.RequiredArgsConstructor;

@InternalService
@RequiredArgsConstructor
public class NotificationProfileResolver {

	private final UserProfileQueryService userProfileQueryService;
	private final WorkspaceMembershipQueryService workspaceMembershipQueryService;

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

		return userProfileQueryService.findCurrentUserProfileImageUrl(recipientUserId)
			.map(profileImageUrl -> new NotificationProfileResponse(
				NotificationProfileSourceType.RECIPIENT,
				profileImageUrl
			))
			.orElseGet(() -> new NotificationProfileResponse(NotificationProfileSourceType.RECIPIENT, null));
	}

	private NotificationProfileResponse subjectWorkspaceMemberProfile(NotificationAlert notificationAlert) {
		if (notificationAlert.getScopeType() != NotificationScopeType.WORKSPACE
			|| notificationAlert.getSubjectType() != NotificationSubjectType.WORKSPACE_MEMBER
			|| notificationAlert.getSubjectId() == null) {
			return new NotificationProfileResponse(NotificationProfileSourceType.SUBJECT, null);
		}

		return workspaceMembershipQueryService
			.findActiveProfileImageUrlByWorkspaceIdAndMemberId(
				notificationAlert.getScopeId(),
				notificationAlert.getSubjectId()
			)
			.map(profileImageUrl -> new NotificationProfileResponse(
				NotificationProfileSourceType.SUBJECT,
				profileImageUrl
			))
			.orElseGet(() -> new NotificationProfileResponse(NotificationProfileSourceType.SUBJECT, null));
	}

	private NotificationProfileResponse actorWorkspaceMemberProfile(NotificationAlert notificationAlert) {
		if (notificationAlert.getScopeType() != NotificationScopeType.WORKSPACE
			|| notificationAlert.getActorType() != NotificationActorType.USER
			|| notificationAlert.getActorId() == null) {
			return new NotificationProfileResponse(NotificationProfileSourceType.ACTOR, null);
		}

		return workspaceMembershipQueryService
			.findActiveProfileImageUrlByWorkspaceIdAndUserId(
				notificationAlert.getScopeId(),
				notificationAlert.getActorId()
			)
			.map(profileImageUrl -> new NotificationProfileResponse(
				NotificationProfileSourceType.ACTOR,
				profileImageUrl
			))
			.orElseGet(() -> new NotificationProfileResponse(NotificationProfileSourceType.ACTOR, null));
	}
}
