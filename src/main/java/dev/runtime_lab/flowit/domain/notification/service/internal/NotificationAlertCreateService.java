package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationRecipient;
import dev.runtime_lab.flowit.domain.notification.event.NotificationAlertCreatedEvent;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationAlertRepository;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationRecipientRepository;
import dev.runtime_lab.flowit.domain.notification.service.internal.command.NotificationAlertCreateCommand;
import dev.runtime_lab.flowit.domain.workspace.service.internal.WorkspaceMembershipQueryService;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

@InternalService
@RequiredArgsConstructor
public class NotificationAlertCreateService {

	private final NotificationAlertRepository notificationAlertRepository;
	private final NotificationRecipientRepository notificationRecipientRepository;
	private final WorkspaceMembershipQueryService workspaceMembershipQueryService;
	private final ApplicationEventPublisher eventPublisher;
	private final Clock clock;

	@Transactional
	public void create(NotificationAlertCreateCommand command) {
		Long createdAt = Instant.now(clock).getEpochSecond();
		NotificationAlert notificationAlert = notificationAlertRepository.save(NotificationAlert.builder()
			.sourceType(command.sourceType())
			.sourceId(command.sourceId())
			.type(command.type())
			.scopeType(command.scopeType())
			.scopeId(command.scopeId())
			.scopeNameSnapshot(command.scopeName())
			.actorType(command.actorType())
			.actorId(command.actorId())
			.actorNameSnapshot(command.actorName())
			.actorProfileImageUrl(command.actorProfileImageUrl())
			.subjectType(command.subjectType())
			.subjectId(command.subjectId())
			.subjectNameSnapshot(command.subjectName())
			.changesJson(command.changesJson())
			.linkType(command.linkType())
			.linkWorkspaceId(command.linkWorkspaceId())
			.occurredAt(command.occurredAt())
			.createdAt(createdAt)
			.build());

		notificationRecipientRepository.saveAll(recipientUserIds(command).stream()
			.distinct()
			.map(userId -> NotificationRecipient.builder()
				.notificationAlert(notificationAlert)
				.userId(userId)
				.createdAt(createdAt)
				.build())
			.toList());

		eventPublisher.publishEvent(NotificationAlertCreatedEvent.from(notificationAlert));
	}

	private List<Long> recipientUserIds(NotificationAlertCreateCommand command) {
		if (command.scopeType() != NotificationScopeType.WORKSPACE) {
			return List.of();
		}

		if (command.type() == NotificationAlertType.WORKSPACE_ACCESS_REVOKED) {
			return removedMemberUserId(command)
				.map(List::of)
				.orElseGet(List::of);
		}

		return workspaceMembershipQueryService.findActiveMemberUserIds(command.scopeId());
	}

	private Optional<Long> removedMemberUserId(NotificationAlertCreateCommand command) {
		if (command.type() != NotificationAlertType.WORKSPACE_ACCESS_REVOKED) {
			return Optional.empty();
		}

		return workspaceMembershipQueryService.findMemberUserId(command.scopeId(), command.subjectId());
	}
}
