package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.event.NotificationAlertCreatedEvent;
import dev.runtime_lab.flowit.global.socket.WebSocketPublisher;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@InternalService
@RequiredArgsConstructor
public class NotificationAlertSocketEventListener {

	private final NotificationAlertSocketDispatchLoader notificationAlertSocketDispatchLoader;
	private final WebSocketPublisher webSocketPublisher;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publishNotification(NotificationAlertCreatedEvent event) {
		notificationAlertSocketDispatchLoader.load(event.notificationAlertId())
			.ifPresent(this::publish);
	}

	private void publish(NotificationAlertSocketDispatch dispatch) {
		dispatch.recipientUserIds()
			.forEach(userId -> webSocketPublisher.publishUserNotification(userId, dispatch.payload()));
	}
}
