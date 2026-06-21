package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.repository.WorkspaceActivityRecordRepository;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertCreateService;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventType;
import dev.runtime_lab.flowit.global.outbox.service.OutboxEventHandler;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@InternalService
@RequiredArgsConstructor
public class WorkspaceActivityNotificationOutboxHandler implements OutboxEventHandler {

	private final WorkspaceActivityRecordRepository workspaceActivityRecordRepository;
	private final WorkspaceMemberActivityNotificationCommandFactory notificationCommandFactory;
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
			.ifPresent(record -> notificationCommandFactory.create(record)
				.forEach(notificationAlertCreateService::create));
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
