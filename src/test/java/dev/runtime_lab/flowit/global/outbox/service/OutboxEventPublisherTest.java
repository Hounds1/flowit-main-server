package dev.runtime_lab.flowit.global.outbox.service;

import dev.runtime_lab.flowit.global.outbox.entity.OutboxEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventStatus;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventType;
import dev.runtime_lab.flowit.global.outbox.event.OutboxEventCreatedEvent;
import dev.runtime_lab.flowit.global.outbox.repository.OutboxEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxEventPublisherTest {

	private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
	private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
	private final Clock clock = Clock.fixed(Instant.ofEpochSecond(1782013300L), ZoneOffset.UTC);
	private final OutboxEventPublisher outboxEventPublisher = new OutboxEventPublisher(
		outboxEventRepository,
		eventPublisher,
		JsonMapper.builder().build(),
		clock
	);

	@Test
	void savesOutboxEventAndPublishesCreatedEvent() {
		ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);

		when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

		outboxEventPublisher.publish(
			OutboxEventType.WORKSPACE_ACTIVITY_NOTIFICATION_REQUESTED,
			Map.of("activityRecordId", 921L)
		);

		verify(outboxEventRepository).save(outboxCaptor.capture());
		OutboxEvent outboxEvent = outboxCaptor.getValue();
		assertEquals(OutboxEventType.WORKSPACE_ACTIVITY_NOTIFICATION_REQUESTED, outboxEvent.getEventType());
		assertEquals(OutboxEventStatus.PENDING, outboxEvent.getStatus());
		assertEquals(1782013300L, outboxEvent.getCreatedAt());
		assertTrue(outboxEvent.getPayloadJson().contains("\"activityRecordId\":921"));
		verify(eventPublisher).publishEvent(any(OutboxEventCreatedEvent.class));
	}
}
