package dev.runtime_lab.flowit.domain.notification.dto;

public record NotificationChangeResponse(
	String element,
	Object from,
	Object to
) {
}
