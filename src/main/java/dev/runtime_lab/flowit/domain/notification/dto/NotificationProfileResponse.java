package dev.runtime_lab.flowit.domain.notification.dto;

public record NotificationProfileResponse(
	NotificationProfileSourceType source,
	String profileImageUrl
) {
}
