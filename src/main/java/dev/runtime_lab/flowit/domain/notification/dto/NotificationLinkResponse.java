package dev.runtime_lab.flowit.domain.notification.dto;

public record NotificationLinkResponse(
	NotificationLinkType type,
	Long workspaceId
) {
}
