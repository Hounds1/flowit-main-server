package dev.runtime_lab.flowit.domain.notification.dto;

public record NotificationAlertSeenResponse(
	long seenAt,
	long seenCount
) {
}
