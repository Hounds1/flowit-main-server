package dev.runtime_lab.flowit.domain.notification.service;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertListResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertReadAllResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertSeenResponse;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationRecipient;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationRecipientRepository;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertResponseAssembler;
import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.user.service.internal.CurrentUserProvider;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;
	private static final int MIN_PAGE_SIZE = 1;

	private final CurrentUserProvider currentUserProvider;
	private final NotificationRecipientRepository notificationRecipientRepository;
	private final NotificationAlertResponseAssembler notificationAlertResponseAssembler;
	private final Clock clock;

	@Transactional(readOnly = true)
	public NotificationAlertListResponse alerts(CurrentUser currentUser, Integer page, Integer size) {
		User requester = currentUserProvider.findActive(currentUser);
		int pageValue = page(page);
		int sizeValue = size(size);

		List<NotificationAlertResponse> alerts = notificationRecipientRepository
			.findVisibleByUserId(requester.getId(), pageValue, sizeValue)
			.stream()
			.map(this::alertResponse)
			.toList();

		long totalCount = notificationRecipientRepository.countVisibleByUserId(requester.getId());
		long unreadCount = notificationRecipientRepository.countVisibleUnreadByUserId(requester.getId());
		long unseenCount = notificationRecipientRepository.countVisibleUnseenByUserId(requester.getId());

		return new NotificationAlertListResponse(alerts, totalCount, unreadCount, unseenCount);
	}

	@Transactional
	public NotificationAlertSeenResponse seen(CurrentUser currentUser) {
		User requester = currentUserProvider.findActive(currentUser);
		long seenAt = Instant.now(clock).getEpochSecond();
		int seenCount = notificationRecipientRepository.markVisibleUnseenAsSeenByUserId(requester.getId(), seenAt);

		return new NotificationAlertSeenResponse(seenAt, seenCount);
	}

	@Transactional
	public NotificationAlertReadAllResponse readAll(CurrentUser currentUser) {
		User requester = currentUserProvider.findActive(currentUser);
		long readAt = Instant.now(clock).getEpochSecond();
		int readCount = notificationRecipientRepository.markVisibleUnreadAsReadByUserId(requester.getId(), readAt);
		notificationRecipientRepository.markVisibleUnseenAsSeenByUserId(requester.getId(), readAt);

		return new NotificationAlertReadAllResponse(readAt, readCount);
	}

	private NotificationAlertResponse alertResponse(NotificationRecipient recipient) {
		return notificationAlertResponseAssembler.toResponse(
			recipient.getNotificationAlert(),
			recipient.getReadAt() != null
		);
	}

	private int page(Integer page) {
		if (page == null || page < 0) {
			return 0;
		}

		return page;
	}

	private int size(Integer size) {
		if (size == null) {
			return DEFAULT_PAGE_SIZE;
		}
		if (size < MIN_PAGE_SIZE) {
			return MIN_PAGE_SIZE;
		}
		if (size > MAX_PAGE_SIZE) {
			return MAX_PAGE_SIZE;
		}

		return size;
	}
}
