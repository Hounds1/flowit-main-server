package dev.runtime_lab.flowit.domain.notification.dto;

public record NotificationActorResponse(
	NotificationActorType type,
	Long id,
	String name
) {
}
