package dev.runtime_lab.flowit.global.outbox.service;

import dev.runtime_lab.flowit.global.outbox.event.OutboxEventCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OutboxEventCreatedListener {

	private final OutboxEventProcessor outboxEventProcessor;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void process(OutboxEventCreatedEvent event) {
		try {
			outboxEventProcessor.process(event.outboxEventId());
		}
		catch (RuntimeException exception) {
			outboxEventProcessor.markFailed(event.outboxEventId(), exception);
		}
	}
}
