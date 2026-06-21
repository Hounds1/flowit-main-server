package dev.runtime_lab.flowit.domain.notification.dto;

public record NotificationSubjectResponse(
	NotificationSubjectType type,
	Long id,
	String name
) {
}
