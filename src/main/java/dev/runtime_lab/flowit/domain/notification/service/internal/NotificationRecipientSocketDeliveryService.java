package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertResponse;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationRecipient;
import dev.runtime_lab.flowit.domain.notification.event.NotificationRecipientDeliveryRequestedEvent;
import dev.runtime_lab.flowit.domain.notification.lock.NotificationRecipientDeliveryLock;
import dev.runtime_lab.flowit.domain.notification.queue.NotificationRecipientDeliveryRetryQueue;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationRecipientRepository;
import dev.runtime_lab.flowit.global.socket.WebSocketPublisher;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@InternalService
@RequiredArgsConstructor
@Slf4j
public class NotificationRecipientSocketDeliveryService {

	private static final int DELIVERY_BATCH_SIZE = 100;
	private static final int MAX_BATCHES_PER_LOCK = 10;

	private final NotificationRecipientRepository notificationRecipientRepository;
	private final NotificationAlertResponseAssembler notificationAlertResponseAssembler;
	private final WebSocketPublisher webSocketPublisher;
	private final NotificationRecipientDeliveryLock notificationRecipientDeliveryLock;
	private final NotificationRecipientDeliveryRetryQueue notificationRecipientDeliveryRetryQueue;
	private final ApplicationEventPublisher eventPublisher;
	private final Clock clock;

	public boolean deliver(Long userId) {
		Objects.requireNonNull(userId, "userId must not be null");

		AtomicBoolean requestFollowUp = new AtomicBoolean(false);
		AtomicBoolean completed = new AtomicBoolean(false);
		boolean locked = notificationRecipientDeliveryLock.executeWithLock(
			userId,
			() -> {
				DeliveryResult deliveryResult = deliverPending(userId);
				requestFollowUp.set(deliveryResult == DeliveryResult.NEEDS_FOLLOW_UP);
				completed.set(deliveryResult != DeliveryResult.FAILED);
			}
		);
		if (!locked) {
			notificationRecipientDeliveryRetryQueue.schedule(userId);
			return false;
		}
		if (requestFollowUp.get()) {
			eventPublisher.publishEvent(new NotificationRecipientDeliveryRequestedEvent(userId));
		}
		return completed.get();
	}

	private DeliveryResult deliverPending(Long userId) {
		boolean processedAny = false;
		for (int batchCount = 0; batchCount < MAX_BATCHES_PER_LOCK; batchCount++) {
			List<NotificationRecipient> recipients = notificationRecipientRepository
				.findPendingSocketDeliveryByUserId(userId, DELIVERY_BATCH_SIZE);
			if (recipients.isEmpty()) {
				return DeliveryResult.IDLE;
			}

			processedAny = true;
			for (NotificationRecipient recipient : recipients) {
				if (!publishAndMark(userId, recipient)) {
					notificationRecipientDeliveryRetryQueue.schedule(userId);
					return DeliveryResult.FAILED;
				}
			}
		}

		return processedAny ? DeliveryResult.NEEDS_FOLLOW_UP : DeliveryResult.IDLE;
	}

	private boolean publishAndMark(Long userId, NotificationRecipient recipient) {
		NotificationAlertResponse payload;
		try {
			payload = notificationAlertResponseAssembler.toResponse(
				recipient.getNotificationAlert(),
				recipient.getReadAt() != null
			);
		}
		catch (RuntimeException exception) {
			log.warn(
				"Failed to assemble notification websocket payload. notificationRecipientId={} userId={}",
				recipient.getId(),
				userId,
				exception
			);
			return false;
		}

		try {
			webSocketPublisher.publishUserNotification(userId, payload);
		}
		catch (RuntimeException exception) {
			log.warn(
				"Failed to publish notification websocket payload. notificationRecipientId={} userId={}",
				recipient.getId(),
				userId,
				exception
			);
			return false;
		}

		try {
			notificationRecipientRepository.markSocketSentIfPending(
				recipient.getId(),
				Instant.now(clock).getEpochSecond()
			);
			return true;
		}
		catch (RuntimeException exception) {
			log.warn(
				"Failed to mark notification websocket delivery. notificationRecipientId={} userId={}",
				recipient.getId(),
				userId,
				exception
			);
			return false;
		}
	}

	private enum DeliveryResult {
		IDLE,
		NEEDS_FOLLOW_UP,
		FAILED
	}
}
