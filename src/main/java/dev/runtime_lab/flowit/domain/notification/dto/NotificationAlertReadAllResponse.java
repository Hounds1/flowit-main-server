package dev.runtime_lab.flowit.domain.notification.dto;

public record NotificationAlertReadAllResponse(
	long readAt,
	long readCount
) {
}
