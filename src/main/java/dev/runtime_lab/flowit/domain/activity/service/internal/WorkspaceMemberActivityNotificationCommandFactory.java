package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordAction;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationSourceType;
import dev.runtime_lab.flowit.domain.notification.service.internal.command.NotificationAlertCreateCommand;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.util.List;
import java.util.Optional;

@InternalService
public class WorkspaceMemberActivityNotificationCommandFactory {

	public List<NotificationAlertCreateCommand> create(WorkspaceActivityRecord record) {
		if (record.getDomain() != ActivityRecordDomain.WORKSPACE_MEMBER) {
			return List.of();
		}

		if (record.getAction() == ActivityRecordAction.REMOVED) {
			return List.of(
				command(record, NotificationAlertType.WORKSPACE_MEMBER_REMOVED, NotificationLinkType.WORKSPACE_MEMBERS),
				command(record, NotificationAlertType.WORKSPACE_ACCESS_REVOKED, NotificationLinkType.NONE)
			);
		}

		return type(record.getAction())
			.map(type -> List.of(command(record, type, NotificationLinkType.WORKSPACE_MEMBERS)))
			.orElseGet(List::of);
	}

	private NotificationAlertCreateCommand command(
		WorkspaceActivityRecord record,
		NotificationAlertType type,
		NotificationLinkType linkType
	) {
		return new NotificationAlertCreateCommand(
			NotificationSourceType.WORKSPACE_ACTIVITY_RECORD,
			record.getId(),
			type,
			NotificationScopeType.WORKSPACE,
			record.getWorkspace().getId(),
			record.getWorkspace().getName(),
			record.getActorUser() == null ? null : NotificationActorType.USER,
			record.getActorUser() == null ? null : record.getActorUser().getId(),
			record.getActorDisplayNameSnapshot(),
			null,
			NotificationSubjectType.WORKSPACE_MEMBER,
			record.getTargetId(),
			record.getTargetDisplayNameSnapshot(),
			record.getChangesJson(),
			linkType,
			linkType == NotificationLinkType.NONE ? null : record.getWorkspace().getId(),
			record.getOccurredAt()
		);
	}

	private Optional<NotificationAlertType> type(ActivityRecordAction action) {
		return switch (action) {
			case JOINED -> Optional.of(NotificationAlertType.WORKSPACE_MEMBER_JOINED);
			case ROLE_CHANGED -> Optional.of(NotificationAlertType.WORKSPACE_MEMBER_ROLE_CHANGED);
			case WITHDRAWN -> Optional.of(NotificationAlertType.WORKSPACE_MEMBER_WITHDRAWN);
			default -> Optional.empty();
		};
	}
}
