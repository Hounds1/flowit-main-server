package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.queue.NotificationRecipientDeliveryRetryQueue;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationRecipientDeliveryRetryWorkerTest {

	private final NotificationRecipientDeliveryRetryQueue notificationRecipientDeliveryRetryQueue =
		mock(NotificationRecipientDeliveryRetryQueue.class);
	private final NotificationRecipientSocketDeliveryService notificationRecipientSocketDeliveryService =
		mock(NotificationRecipientSocketDeliveryService.class);
	private final NotificationRecipientDeliveryRetryWorker worker = new NotificationRecipientDeliveryRetryWorker(
		notificationRecipientDeliveryRetryQueue,
		notificationRecipientSocketDeliveryService
	);

	@Test
	void deliversDueRecipients() {
		when(notificationRecipientDeliveryRetryQueue.pollDue(100)).thenReturn(List.of(34L, 35L));
		when(notificationRecipientSocketDeliveryService.deliver(34L)).thenReturn(true);
		when(notificationRecipientSocketDeliveryService.deliver(35L)).thenReturn(true);

		worker.deliverDueRecipients();

		verify(notificationRecipientSocketDeliveryService).deliver(34L);
		verify(notificationRecipientSocketDeliveryService).deliver(35L);
		verify(notificationRecipientDeliveryRetryQueue).complete(34L);
		verify(notificationRecipientDeliveryRetryQueue).complete(35L);
	}

	@Test
	void skipsDeliveryWhenPollingFails() {
		when(notificationRecipientDeliveryRetryQueue.pollDue(100))
			.thenThrow(new IllegalStateException("redis unavailable"));

		worker.deliverDueRecipients();

		verify(notificationRecipientSocketDeliveryService, never()).deliver(34L);
	}

	@Test
	void reschedulesUserWhenRetryDeliveryFails() {
		when(notificationRecipientDeliveryRetryQueue.pollDue(100)).thenReturn(List.of(34L, 35L));
		when(notificationRecipientSocketDeliveryService.deliver(35L)).thenReturn(true);
		doThrow(new IllegalStateException("broker unavailable"))
			.when(notificationRecipientSocketDeliveryService)
			.deliver(34L);

		worker.deliverDueRecipients();

		verify(notificationRecipientDeliveryRetryQueue).schedule(34L);
		verify(notificationRecipientSocketDeliveryService).deliver(35L);
		verify(notificationRecipientDeliveryRetryQueue).complete(35L);
	}

	@Test
	void keepsUserScheduledWhenRetryDeliveryIsNotCompleted() {
		when(notificationRecipientDeliveryRetryQueue.pollDue(100)).thenReturn(List.of(34L));
		when(notificationRecipientSocketDeliveryService.deliver(34L)).thenReturn(false);

		worker.deliverDueRecipients();

		verify(notificationRecipientDeliveryRetryQueue, never()).complete(34L);
	}
}
