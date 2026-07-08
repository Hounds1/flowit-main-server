package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationChangeResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectResponse;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationRecipient;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@InternalService
@RequiredArgsConstructor
public class NotificationAlertResponseAssembler {

	private final JsonMapper jsonMapper;
	private final NotificationProfileResolver notificationProfileResolver;

	public NotificationAlertResponse toResponse(NotificationRecipient recipient) {
		return toResponse(
			recipient.getNotificationAlert(),
			recipient.getUserId(),
			recipient.getReadAt() != null
		);
	}

	public NotificationAlertResponse toResponse(NotificationAlert notificationAlert, Long recipientUserId, boolean read) {
		return new NotificationAlertResponse(
			notificationAlert.getId(),
			notificationAlert.getType(),
			notificationAlert.getOccurredAt(),
			notificationProfileResolver.resolve(notificationAlert, recipientUserId),
			new NotificationScopeResponse(
				notificationAlert.getScopeType(),
				notificationAlert.getScopeId(),
				notificationAlert.getScopeNameSnapshot()
			),
			new NotificationActorResponse(
				notificationAlert.getActorType(),
				notificationAlert.getActorId(),
				notificationAlert.getActorNameSnapshot()
			),
			new NotificationSubjectResponse(
				notificationAlert.getSubjectType(),
				notificationAlert.getSubjectId(),
				notificationAlert.getSubjectNameSnapshot()
			),
			changes(notificationAlert.getChangesJson()),
			new NotificationLinkResponse(
				notificationAlert.getLinkType(),
				notificationAlert.getLinkWorkspaceId()
			),
			read
		);
	}

	private List<NotificationChangeResponse> changes(String changesJson) {
		try {
			return jsonMapper.readValue(changesJson, new TypeReference<>() {
			});
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Failed to deserialize notification changes.", exception);
		}
	}
}
