package dev.runtime_lab.flowit.domain.notification.service.internal.command;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationSourceType;

public record NotificationAlertCreateCommand(
	NotificationSourceType sourceType,
	Long sourceId,
	NotificationAlertType type,
	NotificationScopeType scopeType,
	Long scopeId,
	String scopeName,
	NotificationActorType actorType,
	Long actorId,
	String actorName,
	String actorProfileImageUrl,
	NotificationSubjectType subjectType,
	Long subjectId,
	String subjectName,
	String changesJson,
	NotificationLinkType linkType,
	Long linkWorkspaceId,
	Long occurredAt
) {
}
