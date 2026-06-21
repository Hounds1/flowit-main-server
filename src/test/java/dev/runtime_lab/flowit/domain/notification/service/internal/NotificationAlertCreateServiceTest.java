package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationRecipient;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationSourceType;
import dev.runtime_lab.flowit.domain.notification.event.NotificationAlertCreatedEvent;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationAlertRepository;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationRecipientRepository;
import dev.runtime_lab.flowit.domain.notification.service.internal.command.NotificationAlertCreateCommand;
import dev.runtime_lab.flowit.domain.workspace.service.internal.WorkspaceMembershipQueryService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationAlertCreateServiceTest {

	private final NotificationAlertRepository notificationAlertRepository = mock(NotificationAlertRepository.class);
	private final NotificationRecipientRepository notificationRecipientRepository =
		mock(NotificationRecipientRepository.class);
	private final WorkspaceMembershipQueryService workspaceMembershipQueryService =
		mock(WorkspaceMembershipQueryService.class);
	private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
	private final Clock clock = Clock.fixed(Instant.ofEpochSecond(1782013300L), ZoneOffset.UTC);
	private final NotificationAlertCreateService service = new NotificationAlertCreateService(
		notificationAlertRepository,
		notificationRecipientRepository,
		workspaceMembershipQueryService,
		eventPublisher,
		clock
	);

	@Test
	void createsNotificationAlertAndRecipientRows() {
		ArgumentCaptor<NotificationAlert> alertCaptor = ArgumentCaptor.forClass(NotificationAlert.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<NotificationRecipient>> recipientCaptor = ArgumentCaptor.forClass(List.class);

		when(notificationAlertRepository.save(any(NotificationAlert.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		when(workspaceMembershipQueryService.findActiveMemberUserIds(12L)).thenReturn(List.of(34L, 35L, 34L));

		service.create(command());

		verify(notificationAlertRepository).save(alertCaptor.capture());
		NotificationAlert alert = alertCaptor.getValue();
		assertEquals(NotificationSourceType.WORKSPACE_ACTIVITY_RECORD, alert.getSourceType());
		assertEquals(921L, alert.getSourceId());
		assertEquals(NotificationAlertType.WORKSPACE_MEMBER_JOINED, alert.getType());
		assertEquals(NotificationScopeType.WORKSPACE, alert.getScopeType());
		assertEquals(12L, alert.getScopeId());
		assertEquals("Flowit", alert.getScopeNameSnapshot());
		assertEquals(1782013200L, alert.getOccurredAt());
		assertEquals(1782013300L, alert.getCreatedAt());

		verify(notificationRecipientRepository).saveAll(recipientCaptor.capture());
		List<NotificationRecipient> recipients = recipientCaptor.getValue();
		assertEquals(2, recipients.size());
		assertEquals(34L, recipients.get(0).getUserId());
		assertEquals(35L, recipients.get(1).getUserId());
		assertEquals(1782013300L, recipients.get(0).getCreatedAt());
		verify(eventPublisher).publishEvent(any(NotificationAlertCreatedEvent.class));
	}

	@Test
	void sendsRemovedMemberNotificationToActiveWorkspaceMembersOnly() {
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<NotificationRecipient>> recipientCaptor = ArgumentCaptor.forClass(List.class);

		when(notificationAlertRepository.save(any(NotificationAlert.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		when(workspaceMembershipQueryService.findActiveMemberUserIds(12L)).thenReturn(List.of(34L, 35L));

		service.create(removedCommand());

		verify(notificationRecipientRepository).saveAll(recipientCaptor.capture());
		List<NotificationRecipient> recipients = recipientCaptor.getValue();
		assertEquals(2, recipients.size());
		assertEquals(34L, recipients.get(0).getUserId());
		assertEquals(35L, recipients.get(1).getUserId());
	}

	@Test
	void sendsAccessRevokedNotificationToRemovedMemberUserOnly() {
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<NotificationRecipient>> recipientCaptor = ArgumentCaptor.forClass(List.class);

		when(notificationAlertRepository.save(any(NotificationAlert.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		when(workspaceMembershipQueryService.findMemberUserId(12L, 55L)).thenReturn(Optional.of(36L));

		service.create(accessRevokedCommand());

		verify(notificationRecipientRepository).saveAll(recipientCaptor.capture());
		List<NotificationRecipient> recipients = recipientCaptor.getValue();
		assertEquals(1, recipients.size());
		assertEquals(36L, recipients.get(0).getUserId());
	}

	private NotificationAlertCreateCommand command() {
		return new NotificationAlertCreateCommand(
			NotificationSourceType.WORKSPACE_ACTIVITY_RECORD,
			921L,
			NotificationAlertType.WORKSPACE_MEMBER_JOINED,
			NotificationScopeType.WORKSPACE,
			12L,
			"Flowit",
			NotificationActorType.USER,
			34L,
			"Actor",
			null,
			NotificationSubjectType.WORKSPACE_MEMBER,
			55L,
			"Target",
			"[]",
			NotificationLinkType.WORKSPACE_MEMBERS,
			12L,
			1782013200L
		);
	}

	private NotificationAlertCreateCommand removedCommand() {
		return new NotificationAlertCreateCommand(
			NotificationSourceType.WORKSPACE_ACTIVITY_RECORD,
			921L,
			NotificationAlertType.WORKSPACE_MEMBER_REMOVED,
			NotificationScopeType.WORKSPACE,
			12L,
			"Flowit",
			NotificationActorType.USER,
			34L,
			"Actor",
			null,
			NotificationSubjectType.WORKSPACE_MEMBER,
			55L,
			"Target",
			"[]",
			NotificationLinkType.WORKSPACE_MEMBERS,
			12L,
			1782013200L
		);
	}

	private NotificationAlertCreateCommand accessRevokedCommand() {
		return new NotificationAlertCreateCommand(
			NotificationSourceType.WORKSPACE_ACTIVITY_RECORD,
			921L,
			NotificationAlertType.WORKSPACE_ACCESS_REVOKED,
			NotificationScopeType.WORKSPACE,
			12L,
			"Flowit",
			NotificationActorType.USER,
			34L,
			"Actor",
			null,
			NotificationSubjectType.WORKSPACE_MEMBER,
			55L,
			"Target",
			"[]",
			NotificationLinkType.NONE,
			null,
			1782013200L
		);
	}
}
