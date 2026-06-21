package dev.runtime_lab.flowit.global.outbox.event;

import dev.runtime_lab.flowit.global.outbox.entity.OutboxEvent;

public record OutboxEventCreatedEvent(
	Long outboxEventId
) {

	public static OutboxEventCreatedEvent from(OutboxEvent event) {
		return new OutboxEventCreatedEvent(event.getId());
	}
}
