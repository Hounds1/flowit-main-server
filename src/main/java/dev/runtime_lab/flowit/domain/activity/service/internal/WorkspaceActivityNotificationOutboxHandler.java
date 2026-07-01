package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.repository.WorkspaceActivityRecordRepository;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertCreateService;
import dev.runtime_lab.flowit.domain.notification.service.internal.command.NotificationAlertCreateCommand;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventType;
import dev.runtime_lab.flowit.global.outbox.service.OutboxEventHandler;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@InternalService
@RequiredArgsConstructor
public class WorkspaceActivityNotificationOutboxHandler implements OutboxEventHandler {

	private static final Set<NotificationAlertType> DISABLED_NOTIFICATION_TYPES = EnumSet.of(
		NotificationAlertType.TASK_DATE_CHANGED,
		NotificationAlertType.TASK_PROGRESS_CHANGED
	);

	private final WorkspaceActivityRecordRepository workspaceActivityRecordRepository;
	private final List<WorkspaceActivityNotificationCommandFactory> notificationCommandFactories;
	private final NotificationAlertCreateService notificationAlertCreateService;
	private final JsonMapper jsonMapper;

	@Override
	public OutboxEventType supports() {
		return OutboxEventType.WORKSPACE_ACTIVITY_NOTIFICATION_REQUESTED;
	}

	@Override
	public void handle(OutboxEvent event) {
		WorkspaceActivityNotificationOutboxPayload payload = readPayload(event.getPayloadJson());

		workspaceActivityRecordRepository.findByIdWithWorkspaceAndActor(payload.activityRecordId())
			.ifPresent(record -> {
				List<NotificationAlertCreateCommand> commands = notificationCommandFactories.stream()
					.flatMap(factory -> factory.create(record).stream())
					.filter(this::isEnabled)
					.toList();
				if (!commands.isEmpty()) {
					notificationAlertCreateService.createAll(commands);
				}
			});
	}

	private boolean isEnabled(NotificationAlertCreateCommand command) {
		return !DISABLED_NOTIFICATION_TYPES.contains(command.type());
	}

	private WorkspaceActivityNotificationOutboxPayload readPayload(String payloadJson) {
		try {
			return jsonMapper.readValue(payloadJson, WorkspaceActivityNotificationOutboxPayload.class);
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Failed to deserialize workspace activity notification outbox payload.", exception);
		}
	}
}
