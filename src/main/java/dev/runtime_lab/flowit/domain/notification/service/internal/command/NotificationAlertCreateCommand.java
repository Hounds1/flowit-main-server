package dev.runtime_lab.flowit.domain.notification.service.internal.command;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationSourceType;
import java.util.List;

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
	NotificationSubjectType subjectType,
	Long subjectId,
	String subjectName,
	String changesJson,
	NotificationLinkType linkType,
	Long linkWorkspaceId,
	Long occurredAt,
	List<Long> recipientUserIds,
	String groupId,
	Integer groupSequence
) {

	private static final int DEFAULT_GROUP_SEQUENCE = 0;

	public NotificationAlertCreateCommand(
		NotificationSourceType sourceType,
		Long sourceId,
		NotificationAlertType type,
		NotificationScopeType scopeType,
		Long scopeId,
		String scopeName,
		NotificationActorType actorType,
		Long actorId,
		String actorName,
		NotificationSubjectType subjectType,
		Long subjectId,
		String subjectName,
		String changesJson,
		NotificationLinkType linkType,
		Long linkWorkspaceId,
		Long occurredAt,
		List<Long> recipientUserIds
	) {
		this(
			sourceType,
			sourceId,
			type,
			scopeType,
			scopeId,
			scopeName,
			actorType,
			actorId,
			actorName,
			subjectType,
			subjectId,
			subjectName,
			changesJson,
			linkType,
			linkWorkspaceId,
			occurredAt,
			recipientUserIds,
			null,
			DEFAULT_GROUP_SEQUENCE
		);
	}

	public NotificationAlertCreateCommand(
		NotificationSourceType sourceType,
		Long sourceId,
		NotificationAlertType type,
		NotificationScopeType scopeType,
		Long scopeId,
		String scopeName,
		NotificationActorType actorType,
		Long actorId,
		String actorName,
		NotificationSubjectType subjectType,
		Long subjectId,
		String subjectName,
		String changesJson,
		NotificationLinkType linkType,
		Long linkWorkspaceId,
		Long occurredAt
	) {
		this(
			sourceType,
			sourceId,
			type,
			scopeType,
			scopeId,
			scopeName,
			actorType,
			actorId,
			actorName,
			subjectType,
			subjectId,
			subjectName,
			changesJson,
			linkType,
			linkWorkspaceId,
			occurredAt,
			null,
			null,
			DEFAULT_GROUP_SEQUENCE
		);
	}

	public NotificationAlertCreateCommand {
		if (recipientUserIds != null) {
			recipientUserIds = List.copyOf(recipientUserIds);
		}
		if (groupSequence == null) {
			groupSequence = DEFAULT_GROUP_SEQUENCE;
		}
	}
}
