package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertResponse;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationAlertRepository;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationRecipientRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NotificationAlertSocketDispatchLoaderTest {

	private final NotificationAlertRepository notificationAlertRepository = mock(NotificationAlertRepository.class);
	private final NotificationRecipientRepository notificationRecipientRepository =
		mock(NotificationRecipientRepository.class);
	private final NotificationAlertResponseAssembler notificationAlertResponseAssembler =
		mock(NotificationAlertResponseAssembler.class);
	private final NotificationAlertSocketDispatchLoader loader = new NotificationAlertSocketDispatchLoader(
		notificationAlertRepository,
		notificationRecipientRepository,
		notificationAlertResponseAssembler
	);

	@Test
	void loadsPayloadAndRecipientUserIdsInReadOnlyTransactionBoundary() {
		NotificationAlert alert = NotificationAlert.builder().id(1L).build();
		NotificationAlertResponse response = mock(NotificationAlertResponse.class);

		when(notificationAlertRepository.findById(1L)).thenReturn(Optional.of(alert));
		when(notificationAlertResponseAssembler.toResponse(alert, false)).thenReturn(response);
		when(notificationRecipientRepository.findVisibleUserIdsByNotificationAlertId(1L))
			.thenReturn(List.of(34L, 35L));

		Optional<NotificationAlertSocketDispatch> dispatch = loader.load(1L);

		assertTrue(dispatch.isPresent());
		assertSame(response, dispatch.orElseThrow().payload());
		assertEquals(List.of(34L, 35L), dispatch.orElseThrow().recipientUserIds());
	}

	@Test
	void returnsEmptyWhenNotificationAlertDoesNotExist() {
		when(notificationAlertRepository.findById(1L)).thenReturn(Optional.empty());

		Optional<NotificationAlertSocketDispatch> dispatch = loader.load(1L);

		assertTrue(dispatch.isEmpty());
		verifyNoInteractions(notificationRecipientRepository, notificationAlertResponseAssembler);
	}
}
