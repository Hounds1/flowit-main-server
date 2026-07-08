package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertResponse;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationRecipient;
import dev.runtime_lab.flowit.domain.notification.event.NotificationRecipientDeliveryRequestedEvent;
import dev.runtime_lab.flowit.domain.notification.lock.NotificationRecipientDeliveryLock;
import dev.runtime_lab.flowit.domain.notification.queue.NotificationRecipientDeliveryRetryQueue;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationRecipientRepository;
import dev.runtime_lab.flowit.global.socket.WebSocketPublisher;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
class NotificationRecipientSocketDeliveryServiceTest {

	private final NotificationRecipientRepository notificationRecipientRepository =
		mock(NotificationRecipientRepository.class);
	private final NotificationAlertResponseAssembler notificationAlertResponseAssembler =
		mock(NotificationAlertResponseAssembler.class);
	private final WebSocketPublisher webSocketPublisher = mock(WebSocketPublisher.class);
	private final NotificationRecipientDeliveryRetryQueue notificationRecipientDeliveryRetryQueue =
		mock(NotificationRecipientDeliveryRetryQueue.class);
	private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
	private final Clock clock = Clock.fixed(Instant.ofEpochSecond(1782013300L), ZoneOffset.UTC);

	@Test
	void publishesPendingNotificationsInRepositoryOrderAndMarksThemSent() {
		NotificationRecipient first = recipient(11L, alert(1L), null);
		NotificationRecipient second = recipient(12L, alert(2L), 1782013200L);
		NotificationAlertResponse firstResponse = mock(NotificationAlertResponse.class);
		NotificationAlertResponse secondResponse = mock(NotificationAlertResponse.class);
		NotificationRecipientSocketDeliveryService service = service(executingLock());

		when(notificationRecipientRepository.findPendingSocketDeliveryByUserId(34L, 100))
			.thenReturn(List.of(first, second), List.of());
		when(notificationAlertResponseAssembler.toResponse(first)).thenReturn(firstResponse);
		when(notificationAlertResponseAssembler.toResponse(second)).thenReturn(secondResponse);

		boolean completed = service.deliver(34L);

		assertTrue(completed);
		InOrder inOrder = inOrder(webSocketPublisher, notificationRecipientRepository);
		inOrder.verify(webSocketPublisher).publishUserNotification(34L, firstResponse);
		inOrder.verify(notificationRecipientRepository).markSocketSentIfPending(11L, 1782013300L);
		inOrder.verify(webSocketPublisher).publishUserNotification(34L, secondResponse);
		inOrder.verify(notificationRecipientRepository).markSocketSentIfPending(12L, 1782013300L);
	}

	@Test
	void stopsCurrentRecipientStreamWhenPublishFails() {
		NotificationRecipient first = recipient(11L, alert(1L), null);
		NotificationRecipient second = recipient(12L, alert(2L), null);
		NotificationAlertResponse firstResponse = mock(NotificationAlertResponse.class);
		NotificationAlertResponse secondResponse = mock(NotificationAlertResponse.class);
		NotificationRecipientSocketDeliveryService service = service(executingLock());

		when(notificationRecipientRepository.findPendingSocketDeliveryByUserId(34L, 100))
			.thenReturn(List.of(first, second));
		when(notificationAlertResponseAssembler.toResponse(first)).thenReturn(firstResponse);
		when(notificationAlertResponseAssembler.toResponse(second)).thenReturn(secondResponse);
		doThrow(new IllegalStateException("broker down"))
			.when(webSocketPublisher)
			.publishUserNotification(34L, firstResponse);

		boolean completed = service.deliver(34L);

		assertFalse(completed);
		verify(webSocketPublisher).publishUserNotification(34L, firstResponse);
		verify(webSocketPublisher, never()).publishUserNotification(34L, secondResponse);
		verify(notificationRecipientRepository, never()).markSocketSentIfPending(anyLong(), anyLong());
		verify(notificationRecipientDeliveryRetryQueue).schedule(34L);
	}

	@Test
	void stopsCurrentRecipientStreamWhenPayloadAssemblyFails() {
		NotificationRecipient first = recipient(11L, alert(1L), null);
		NotificationRecipient second = recipient(12L, alert(2L), null);
		NotificationRecipientSocketDeliveryService service = service(executingLock());

		when(notificationRecipientRepository.findPendingSocketDeliveryByUserId(34L, 100))
			.thenReturn(List.of(first, second));
		when(notificationAlertResponseAssembler.toResponse(first)).thenThrow(new IllegalStateException("invalid changes"));

		boolean completed = service.deliver(34L);

		assertFalse(completed);
		verifyNoInteractions(webSocketPublisher);
		verify(notificationAlertResponseAssembler, never()).toResponse(second);
		verify(notificationRecipientRepository, never()).markSocketSentIfPending(anyLong(), anyLong());
		verify(notificationRecipientDeliveryRetryQueue).schedule(34L);
	}

	@Test
	void schedulesDelayedRetryWhenRecipientLockIsNotAcquired() {
		NotificationRecipientSocketDeliveryService service = service((userId, action) -> false);

		boolean completed = service.deliver(34L);

		assertFalse(completed);
		verify(notificationRecipientDeliveryRetryQueue).schedule(34L);
		verifyNoInteractions(notificationRecipientRepository, webSocketPublisher, eventPublisher);
	}

	@Test
	void requestsAnotherDeliveryPassWhenMaxBatchesAreReached() {
		List<NotificationRecipient> recipients = IntStream.rangeClosed(1, 100)
			.mapToObj(index -> recipient((long) index, alert((long) index), null))
			.toList();
		NotificationAlertResponse response = mock(NotificationAlertResponse.class);
		NotificationRecipientSocketDeliveryService service = service(executingLock());
		AtomicInteger queryCount = new AtomicInteger();

		when(notificationRecipientRepository.findPendingSocketDeliveryByUserId(34L, 100))
			.thenAnswer(invocation -> queryCount.getAndIncrement() < 10 ? recipients : List.of());
		when(notificationAlertResponseAssembler.toResponse(any(NotificationRecipient.class))).thenReturn(response);

		boolean completed = service.deliver(34L);

		assertTrue(completed);
		verify(eventPublisher).publishEvent(new NotificationRecipientDeliveryRequestedEvent(34L));
	}

	private NotificationRecipientSocketDeliveryService service(NotificationRecipientDeliveryLock lock) {
		return new NotificationRecipientSocketDeliveryService(
			notificationRecipientRepository,
			notificationAlertResponseAssembler,
			webSocketPublisher,
			lock,
			notificationRecipientDeliveryRetryQueue,
			eventPublisher,
			clock
		);
	}

	private NotificationRecipientDeliveryLock executingLock() {
		return (userId, action) -> {
			action.run();
			return true;
		};
	}

	private NotificationRecipient recipient(Long id, NotificationAlert alert, Long readAt) {
		return NotificationRecipient.builder()
			.id(id)
			.notificationAlert(alert)
			.userId(34L)
			.createdAt(1782013300L)
			.readAt(readAt)
			.build();
	}

	private NotificationAlert alert(Long id) {
		return NotificationAlert.builder()
			.id(id)
			.build();
	}
}
