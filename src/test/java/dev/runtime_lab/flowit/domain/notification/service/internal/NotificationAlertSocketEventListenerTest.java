package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.event.NotificationRecipientDeliveryRequestedEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationAlertSocketEventListenerTest {

	private final NotificationRecipientSocketDeliveryService notificationRecipientSocketDeliveryService =
		mock(NotificationRecipientSocketDeliveryService.class);
	private final NotificationAlertSocketEventListener listener = new NotificationAlertSocketEventListener(
		notificationRecipientSocketDeliveryService
	);

	@Test
	void requestsRecipientNotificationDelivery() {
		listener.publishNotification(new NotificationRecipientDeliveryRequestedEvent(34L));

		verify(notificationRecipientSocketDeliveryService).deliver(34L);
	}

	@Test
	void doesNotPropagateDeliveryFailure() {
		doThrow(new IllegalStateException("delivery failed"))
			.when(notificationRecipientSocketDeliveryService)
			.deliver(34L);

		assertDoesNotThrow(() -> listener.publishNotification(new NotificationRecipientDeliveryRequestedEvent(34L)));

		verify(notificationRecipientSocketDeliveryService).deliver(34L);
	}
}
