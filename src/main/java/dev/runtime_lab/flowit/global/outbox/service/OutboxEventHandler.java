package dev.runtime_lab.flowit.global.outbox.service;

import dev.runtime_lab.flowit.global.outbox.entity.OutboxEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventType;

public interface OutboxEventHandler {

	OutboxEventType supports();

	void handle(OutboxEvent event);
}
