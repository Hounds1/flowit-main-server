package dev.runtime_lab.flowit.domain.notification.dto;

import dev.runtime_lab.flowit.global.socket.dto.WebSocketPayload;
import java.util.List;

public record NotificationAlertResponse(
	Long id,
	NotificationAlertType type,
	Long occurredAt,
	NotificationProfileResponse profile,
	NotificationScopeResponse scope,
	NotificationActorResponse actor,
	NotificationSubjectResponse subject,
	List<NotificationChangeResponse> changes,
	NotificationLinkResponse link,
	boolean read
) implements WebSocketPayload {
}
