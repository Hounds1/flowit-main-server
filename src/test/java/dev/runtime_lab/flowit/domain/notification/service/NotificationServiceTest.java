package dev.runtime_lab.flowit.domain.notification.service;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertListResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertReadAllResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertSeenResponse;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationRecipient;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationRecipientRepository;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertResponseAssembler;
import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.user.entity.UserStatus;
import dev.runtime_lab.flowit.domain.user.service.internal.CurrentUserProvider;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

	private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
	private final NotificationRecipientRepository notificationRecipientRepository =
		mock(NotificationRecipientRepository.class);
	private final NotificationAlertResponseAssembler notificationAlertResponseAssembler =
		mock(NotificationAlertResponseAssembler.class);
	private final Clock clock = Clock.fixed(Instant.ofEpochSecond(1782013300L), ZoneOffset.UTC);
	private final NotificationService notificationService = new NotificationService(
		currentUserProvider,
		notificationRecipientRepository,
		notificationAlertResponseAssembler,
		clock
	);

	@Test
	void alertsReturnCurrentUserNotificationsWithReadStateAndCounts() {
		CurrentUser currentUser = new CurrentUser(7L, "user@example.com", "User");
		NotificationAlert unreadAlert = NotificationAlert.builder().id(1L).build();
		NotificationAlert readAlert = NotificationAlert.builder().id(2L).build();
		NotificationRecipient unreadRecipient = NotificationRecipient.builder()
			.notificationAlert(unreadAlert)
			.userId(7L)
			.createdAt(10L)
			.build();
		NotificationRecipient readRecipient = NotificationRecipient.builder()
			.notificationAlert(readAlert)
			.userId(7L)
			.createdAt(9L)
			.readAt(11L)
			.build();
		NotificationAlertResponse unreadResponse = mock(NotificationAlertResponse.class);
		NotificationAlertResponse readResponse = mock(NotificationAlertResponse.class);

		when(currentUserProvider.findActive(currentUser)).thenReturn(activeUser());
		when(notificationRecipientRepository.findVisibleByUserId(7L, 0, 20))
			.thenReturn(List.of(unreadRecipient, readRecipient));
		when(notificationRecipientRepository.countVisibleByUserId(7L)).thenReturn(2L);
		when(notificationRecipientRepository.countVisibleUnreadByUserId(7L)).thenReturn(1L);
		when(notificationRecipientRepository.countVisibleUnseenByUserId(7L)).thenReturn(1L);
		when(notificationAlertResponseAssembler.toResponse(unreadAlert, false)).thenReturn(unreadResponse);
		when(notificationAlertResponseAssembler.toResponse(readAlert, true)).thenReturn(readResponse);

		NotificationAlertListResponse response = notificationService.alerts(currentUser, null, null);

		assertEquals(List.of(unreadResponse, readResponse), response.items());
		assertEquals(2L, response.totalCount());
		assertEquals(1L, response.unreadCount());
		assertEquals(1L, response.unseenCount());
		verify(currentUserProvider).findActive(currentUser);
	}

	@Test
	void seenMarksVisibleUnseenNotificationsWithCurrentTime() {
		CurrentUser currentUser = new CurrentUser(7L, "user@example.com", "User");

		when(currentUserProvider.findActive(currentUser)).thenReturn(activeUser());
		when(notificationRecipientRepository.markVisibleUnseenAsSeenByUserId(7L, 1782013300L))
			.thenReturn(2);

		NotificationAlertSeenResponse response = notificationService.seen(currentUser);

		assertEquals(1782013300L, response.seenAt());
		assertEquals(2L, response.seenCount());
		verify(notificationRecipientRepository).markVisibleUnseenAsSeenByUserId(7L, 1782013300L);
	}

	@Test
	void readAllMarksVisibleUnreadNotificationsAndClearsUnseenWithCurrentTime() {
		CurrentUser currentUser = new CurrentUser(7L, "user@example.com", "User");

		when(currentUserProvider.findActive(currentUser)).thenReturn(activeUser());
		when(notificationRecipientRepository.markVisibleUnreadAsReadByUserId(7L, 1782013300L))
			.thenReturn(3);

		NotificationAlertReadAllResponse response = notificationService.readAll(currentUser);

		assertEquals(1782013300L, response.readAt());
		assertEquals(3L, response.readCount());
		verify(notificationRecipientRepository).markVisibleUnreadAsReadByUserId(7L, 1782013300L);
		verify(notificationRecipientRepository).markVisibleUnseenAsSeenByUserId(7L, 1782013300L);
	}

	private User activeUser() {
		return User.builder()
			.id(7L)
			.email("user@example.com")
			.passwordHash("hash")
			.name("User")
			.status(UserStatus.ACTIVE)
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}
}
