package dev.runtime_lab.flowit.global.outbox.service;

import dev.runtime_lab.flowit.global.outbox.entity.OutboxEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventStatus;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventType;
import dev.runtime_lab.flowit.global.outbox.event.OutboxEventCreatedEvent;
import dev.runtime_lab.flowit.global.outbox.repository.OutboxEventRepository;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class OutboxEventPublisher {

	private final OutboxEventRepository outboxEventRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final JsonMapper jsonMapper;
	private final Clock clock;

	public void publish(OutboxEventType eventType, Object payload) {
		Long now = Instant.now(clock).getEpochSecond();
		OutboxEvent event = outboxEventRepository.save(OutboxEvent.builder()
			.eventType(eventType)
			.payloadJson(writePayload(payload))
			.status(OutboxEventStatus.PENDING)
			.createdAt(now)
			.updatedAt(now)
			.build());

		eventPublisher.publishEvent(OutboxEventCreatedEvent.from(event));
	}

	private String writePayload(Object payload) {
		try {
			return jsonMapper.writeValueAsString(payload);
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Failed to serialize outbox event payload.", exception);
		}
	}
}
