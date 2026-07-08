package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationRecipient;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationSourceType;
import dev.runtime_lab.flowit.domain.notification.event.NotificationRecipientDeliveryRequestedEvent;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
		assertNull(alert.getGroupId());
		assertEquals(0, alert.getGroupSequence());
		assertEquals(1782013300L, alert.getCreatedAt());

		verify(notificationRecipientRepository).saveAll(recipientCaptor.capture());
		List<NotificationRecipient> recipients = recipientCaptor.getValue();
		assertEquals(2, recipients.size());
		assertEquals(34L, recipients.get(0).getUserId());
		assertEquals(35L, recipients.get(1).getUserId());
		assertEquals(1782013300L, recipients.get(0).getCreatedAt());
		verify(eventPublisher, times(2)).publishEvent(any(NotificationRecipientDeliveryRequestedEvent.class));
	}

	@Test
	void createsGroupedNotificationsWithSharedCreatedAtAndSequenceOrder() {
		ArgumentCaptor<NotificationAlert> alertCaptor = ArgumentCaptor.forClass(NotificationAlert.class);
		ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

		when(notificationAlertRepository.save(any(NotificationAlert.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		service.createAll(List.of(
			groupedTaskCommand(NotificationAlertType.TASK_CREATED, 1),
			groupedTaskCommand(NotificationAlertType.TASK_ASSIGNED, 2)
		));

		verify(notificationAlertRepository, times(2)).save(alertCaptor.capture());
		List<NotificationAlert> alerts = alertCaptor.getAllValues();
		assertEquals(NotificationAlertType.TASK_CREATED, alerts.get(0).getType());
		assertEquals("WORKSPACE_ACTIVITY_RECORD:921", alerts.get(0).getGroupId());
		assertEquals(1, alerts.get(0).getGroupSequence());
		assertEquals(1782013300L, alerts.get(0).getCreatedAt());
		assertEquals(NotificationAlertType.TASK_ASSIGNED, alerts.get(1).getType());
		assertEquals("WORKSPACE_ACTIVITY_RECORD:921", alerts.get(1).getGroupId());
		assertEquals(2, alerts.get(1).getGroupSequence());
		assertEquals(1782013300L, alerts.get(1).getCreatedAt());
		verify(eventPublisher).publishEvent(eventCaptor.capture());
		NotificationRecipientDeliveryRequestedEvent event =
			(NotificationRecipientDeliveryRequestedEvent) eventCaptor.getValue();
		assertEquals(44L, event.userId());
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

	@Test
	void usesExplicitRecipientUserIdsWhenProvided() {
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<NotificationRecipient>> recipientCaptor = ArgumentCaptor.forClass(List.class);

		when(notificationAlertRepository.save(any(NotificationAlert.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		service.create(new NotificationAlertCreateCommand(
			NotificationSourceType.WORKSPACE_ACTIVITY_RECORD,
			921L,
			NotificationAlertType.TASK_DATE_CHANGED,
			NotificationScopeType.WORKSPACE,
			12L,
			"Flowit",
			NotificationActorType.USER,
			34L,
			"Actor",
			NotificationSubjectType.TASK,
			55L,
			"Task",
			"[]",
			NotificationLinkType.TASK_DETAIL,
			12L,
			1782013200L,
			List.of(44L, 45L, 44L)
		));

		verify(workspaceMembershipQueryService, never()).findActiveMemberUserIds(12L);
		verify(notificationRecipientRepository).saveAll(recipientCaptor.capture());
		assertEquals(
			List.of(44L, 45L),
			recipientCaptor.getValue().stream()
				.map(NotificationRecipient::getUserId)
				.toList()
		);
	}

	@Test
	void skipsAlertWhenNoRecipientsResolved() {
		service.create(new NotificationAlertCreateCommand(
			NotificationSourceType.WORKSPACE_ACTIVITY_RECORD,
			921L,
			NotificationAlertType.TASK_DATE_CHANGED,
			NotificationScopeType.WORKSPACE,
			12L,
			"Flowit",
			NotificationActorType.USER,
			34L,
			"Actor",
			NotificationSubjectType.TASK,
			55L,
			"Task",
			"[]",
			NotificationLinkType.TASK_DETAIL,
			12L,
			1782013200L,
			List.of()
		));

		verify(notificationAlertRepository, never()).save(any(NotificationAlert.class));
		verify(notificationRecipientRepository, never()).saveAll(any());
	}

	private NotificationAlertCreateCommand groupedTaskCommand(NotificationAlertType type, Integer groupSequence) {
		return new NotificationAlertCreateCommand(
			NotificationSourceType.WORKSPACE_ACTIVITY_RECORD,
			921L,
			type,
			NotificationScopeType.WORKSPACE,
			12L,
			"Flowit",
			NotificationActorType.USER,
			34L,
			"Actor",
			NotificationSubjectType.TASK,
			55L,
			"Task",
			"[]",
			NotificationLinkType.TASK_DETAIL,
			12L,
			1782013200L,
			List.of(44L),
			"WORKSPACE_ACTIVITY_RECORD:921",
			groupSequence
		);
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
