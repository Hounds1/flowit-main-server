package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.queue.NotificationRecipientDeliveryRetryQueue;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@InternalService
@RequiredArgsConstructor
@Slf4j
public class NotificationRecipientDeliveryRetryWorker {

	private static final int RETRY_BATCH_SIZE = 100;

	private final NotificationRecipientDeliveryRetryQueue notificationRecipientDeliveryRetryQueue;
	private final NotificationRecipientSocketDeliveryService notificationRecipientSocketDeliveryService;

	@Scheduled(fixedDelayString = "${flowit.notification.recipient-delivery.retry.fixed-delay:1000}")
	public void deliverDueRecipients() {
		List<Long> userIds;
		try {
			userIds = notificationRecipientDeliveryRetryQueue.pollDue(RETRY_BATCH_SIZE);
		}
		catch (RuntimeException exception) {
			log.warn("Failed to poll notification recipient delivery retry queue.", exception);
			return;
		}

		for (Long userId : userIds) {
			deliver(userId);
		}
	}

	private void deliver(Long userId) {
		try {
			boolean completed = notificationRecipientSocketDeliveryService.deliver(userId);
			if (completed) {
				notificationRecipientDeliveryRetryQueue.complete(userId);
			}
		}
		catch (RuntimeException exception) {
			log.warn("Failed to retry notification recipient delivery. userId={}", userId, exception);
			notificationRecipientDeliveryRetryQueue.schedule(userId);
		}
	}
}
