package dev.runtime_lab.flowit.domain.notification.lock;

public interface NotificationRecipientDeliveryLock {

	boolean executeWithLock(Long userId, Runnable action);
}
