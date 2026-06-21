package dev.runtime_lab.flowit.domain.notification.event;

import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;

public record NotificationAlertCreatedEvent(
	Long notificationAlertId,
	Long createdAt
) {

	public static NotificationAlertCreatedEvent from(NotificationAlert notificationAlert) {
		return new NotificationAlertCreatedEvent(notificationAlert.getId(), notificationAlert.getCreatedAt());
	}
}
