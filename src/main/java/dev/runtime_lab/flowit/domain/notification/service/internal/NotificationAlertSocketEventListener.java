package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.event.NotificationRecipientDeliveryRequestedEvent;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@InternalService
@RequiredArgsConstructor
@Slf4j
public class NotificationAlertSocketEventListener {

	private final NotificationRecipientSocketDeliveryService notificationRecipientSocketDeliveryService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void publishNotification(NotificationRecipientDeliveryRequestedEvent event) {
		try {
			notificationRecipientSocketDeliveryService.deliver(event.userId());
		}
		catch (RuntimeException exception) {
			log.warn(
				"Failed to deliver notification websocket stream. userId={}",
				event.userId(),
				exception
			);
		}
	}
}
