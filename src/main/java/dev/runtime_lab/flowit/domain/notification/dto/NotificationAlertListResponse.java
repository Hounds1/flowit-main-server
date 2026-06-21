package dev.runtime_lab.flowit.domain.notification.dto;

import java.util.List;

public record NotificationAlertListResponse(
	List<NotificationAlertResponse> items,
	long totalCount,
	long unreadCount,
	long unseenCount
) {

	public NotificationAlertListResponse {
		items = List.copyOf(items);
	}
}
