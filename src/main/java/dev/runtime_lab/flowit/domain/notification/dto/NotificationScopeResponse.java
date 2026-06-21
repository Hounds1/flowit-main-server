package dev.runtime_lab.flowit.domain.notification.dto;

public record NotificationScopeResponse(
	NotificationScopeType type,
	Long id,
	String name
) {
}
