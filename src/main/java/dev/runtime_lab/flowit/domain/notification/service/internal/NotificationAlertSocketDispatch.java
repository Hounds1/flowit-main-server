package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertResponse;
import java.util.List;

record NotificationAlertSocketDispatch(
	NotificationAlertResponse payload,
	List<Long> recipientUserIds
) {
	NotificationAlertSocketDispatch {
		recipientUserIds = List.copyOf(recipientUserIds);
	}
}
