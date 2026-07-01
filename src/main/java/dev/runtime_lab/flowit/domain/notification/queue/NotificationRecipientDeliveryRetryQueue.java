package dev.runtime_lab.flowit.domain.notification.queue;

import java.util.List;

public interface NotificationRecipientDeliveryRetryQueue {

	void schedule(Long userId);

	List<Long> pollDue(int size);

	void complete(Long userId);
}
