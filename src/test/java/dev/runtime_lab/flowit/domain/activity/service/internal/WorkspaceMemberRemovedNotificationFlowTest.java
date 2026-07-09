package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordAction;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityTargetType;
import dev.runtime_lab.flowit.domain.activity.entity.ActivityRecordSourceType;
import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileSourceType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationRecipient;
import dev.runtime_lab.flowit.domain.notification.event.NotificationRecipientDeliveryRequestedEvent;
import dev.runtime_lab.flowit.domain.notification.queue.NotificationRecipientDeliveryRetryQueue;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationAlertRepository;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationRecipientRepository;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertCreateService;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertResponseAssembler;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationProfileResolver;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationRecipientSocketDeliveryService;
import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.workspace.entity.Workspace;
import dev.runtime_lab.flowit.domain.workspace.service.internal.WorkspaceMembershipQueryService;
import dev.runtime_lab.flowit.global.socket.WebSocketPublisher;
import dev.runtime_lab.flowit.global.socket.dto.WebSocketPayload;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkspaceMemberRemovedNotificationFlowTest {

	private final WorkspaceMemberActivityNotificationCommandFactory commandFactory =
		new WorkspaceMemberActivityNotificationCommandFactory();
	private final NotificationAlertRepository notificationAlertRepository = mock(NotificationAlertRepository.class);
	private final NotificationRecipientRepository notificationRecipientRepository =
		mock(NotificationRecipientRepository.class);
	private final WorkspaceMembershipQueryService workspaceMembershipQueryService =
		mock(WorkspaceMembershipQueryService.class);
	private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
	private final ApplicationEventPublisher deliveryEventPublisher = mock(ApplicationEventPublisher.class);
	private final WebSocketPublisher webSocketPublisher = mock(WebSocketPublisher.class);
	private final NotificationRecipientDeliveryRetryQueue notificationRecipientDeliveryRetryQueue =
		mock(NotificationRecipientDeliveryRetryQueue.class);
	private final NotificationProfileResolver notificationProfileResolver = mock(NotificationProfileResolver.class);
	private final Clock clock = Clock.fixed(Instant.ofEpochSecond(1782013300L), ZoneOffset.UTC);
	private final NotificationAlertResponseAssembler responseAssembler =
		new NotificationAlertResponseAssembler(JsonMapper.builder().build(), notificationProfileResolver);
	private final NotificationAlertCreateService createService = new NotificationAlertCreateService(
		notificationAlertRepository,
		notificationRecipientRepository,
		workspaceMembershipQueryService,
		eventPublisher,
		clock
	);
	private final NotificationRecipientSocketDeliveryService socketDeliveryService =
		new NotificationRecipientSocketDeliveryService(
			notificationRecipientRepository,
			responseAssembler,
			webSocketPublisher,
			(userId, action) -> {
				action.run();
				return true;
			},
			notificationRecipientDeliveryRetryQueue,
			deliveryEventPublisher,
			clock
	);

	@Test
	void removedMemberActivityCreatesPerspectiveSpecificAlertsAndPublishesDifferentPayloads() {
		Map<Long, NotificationAlert> savedAlerts = new LinkedHashMap<>();
		Map<Long, List<NotificationRecipient>> recipientsByUserId = new LinkedHashMap<>();
		Map<Long, List<Long>> recipientUserIdsByAlertId = new LinkedHashMap<>();
		List<NotificationRecipientDeliveryRequestedEvent> requestedEvents = new ArrayList<>();
		Set<Long> socketSentRecipientIds = new HashSet<>();
		AtomicLong alertIdSequence = new AtomicLong(100L);
		AtomicLong recipientIdSequence = new AtomicLong(200L);

		when(notificationAlertRepository.save(any(NotificationAlert.class)))
			.thenAnswer(invocation -> {
				NotificationAlert alert = invocation.getArgument(0);
				ReflectionTestUtils.setField(alert, "id", alertIdSequence.getAndIncrement());
				savedAlerts.put(alert.getId(), alert);
				return alert;
			});
		when(notificationAlertRepository.findById(anyLong()))
			.thenAnswer(invocation -> Optional.ofNullable(savedAlerts.get(invocation.getArgument(0))));
		when(notificationRecipientRepository.saveAll(any()))
			.thenAnswer(invocation -> {
				@SuppressWarnings("unchecked")
				List<NotificationRecipient> recipients = invocation.getArgument(0);
				recipients.forEach(recipient -> {
					ReflectionTestUtils.setField(recipient, "id", recipientIdSequence.getAndIncrement());
					recipientsByUserId.computeIfAbsent(recipient.getUserId(), ignored -> new ArrayList<>())
						.add(recipient);
				});
				if (!recipients.isEmpty()) {
					recipientUserIdsByAlertId.put(
						recipients.get(0).getNotificationAlert().getId(),
						recipients.stream()
							.map(NotificationRecipient::getUserId)
							.toList()
					);
				}
				return recipients;
			});
		when(notificationRecipientRepository.findPendingSocketDeliveryByUserId(anyLong(), anyInt()))
			.thenAnswer(invocation -> recipientsByUserId.getOrDefault(invocation.getArgument(0), List.of())
				.stream()
				.filter(recipient -> !socketSentRecipientIds.contains(recipient.getId()))
				.toList());
		when(notificationRecipientRepository.markSocketSentIfPending(anyLong(), anyLong()))
			.thenAnswer(invocation -> {
				socketSentRecipientIds.add(invocation.getArgument(0));
				return 1;
			});
		when(workspaceMembershipQueryService.findActiveMemberUserIds(12L)).thenReturn(List.of(34L, 35L));
		when(workspaceMembershipQueryService.findMemberUserId(12L, 55L)).thenReturn(Optional.of(36L));
		when(notificationProfileResolver.resolve(any(NotificationAlert.class), eq(34L)))
			.thenReturn(new NotificationProfileResponse(NotificationProfileSourceType.ACTOR, "Actor", null));
		when(notificationProfileResolver.resolve(any(NotificationAlert.class), eq(35L)))
			.thenReturn(new NotificationProfileResponse(NotificationProfileSourceType.ACTOR, "Actor", null));
		when(notificationProfileResolver.resolve(any(NotificationAlert.class), eq(36L)))
			.thenReturn(new NotificationProfileResponse(NotificationProfileSourceType.RECIPIENT, "Target", null));
		doAnswer(invocation -> {
			Object event = invocation.getArgument(0);
			if (event instanceof NotificationRecipientDeliveryRequestedEvent deliveryEvent) {
				requestedEvents.add(deliveryEvent);
			}
			return null;
		}).when(eventPublisher).publishEvent(any(Object.class));

		commandFactory.create(removedActivity()).forEach(createService::create);
		requestedEvents.forEach(event -> socketDeliveryService.deliver(event.userId()));

		assertEquals(
			List.of(NotificationAlertType.WORKSPACE_MEMBER_REMOVED, NotificationAlertType.WORKSPACE_ACCESS_REVOKED),
			savedAlerts.values().stream()
				.map(NotificationAlert::getType)
				.toList()
		);
		assertEquals(
			List.of(34L, 35L),
			recipientUserIdsByAlertId.get(alertIdOf(savedAlerts, NotificationAlertType.WORKSPACE_MEMBER_REMOVED))
		);
		assertEquals(
			List.of(36L),
			recipientUserIdsByAlertId.get(alertIdOf(savedAlerts, NotificationAlertType.WORKSPACE_ACCESS_REVOKED))
		);

		ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<WebSocketPayload> payloadCaptor = ArgumentCaptor.forClass(WebSocketPayload.class);
		verify(webSocketPublisher, times(3)).publishUserNotification(userIdCaptor.capture(), payloadCaptor.capture());

		Map<Long, NotificationAlertType> sentTypesByUserId = new LinkedHashMap<>();
		Map<Long, NotificationProfileSourceType> sentProfileSourcesByUserId = new LinkedHashMap<>();
		Map<Long, String> sentProfileNamesByUserId = new LinkedHashMap<>();
		for (int index = 0; index < userIdCaptor.getAllValues().size(); index++) {
			NotificationAlertResponse response = (NotificationAlertResponse) payloadCaptor.getAllValues().get(index);
			sentTypesByUserId.put(userIdCaptor.getAllValues().get(index), response.type());
			sentProfileSourcesByUserId.put(userIdCaptor.getAllValues().get(index), response.profile().source());
			sentProfileNamesByUserId.put(userIdCaptor.getAllValues().get(index), response.profile().displayName());
		}

		assertEquals(NotificationAlertType.WORKSPACE_MEMBER_REMOVED, sentTypesByUserId.get(34L));
		assertEquals(NotificationAlertType.WORKSPACE_MEMBER_REMOVED, sentTypesByUserId.get(35L));
		assertEquals(NotificationAlertType.WORKSPACE_ACCESS_REVOKED, sentTypesByUserId.get(36L));
		assertEquals(NotificationProfileSourceType.ACTOR, sentProfileSourcesByUserId.get(34L));
		assertEquals("Actor", sentProfileNamesByUserId.get(34L));
		assertEquals(NotificationProfileSourceType.ACTOR, sentProfileSourcesByUserId.get(35L));
		assertEquals("Actor", sentProfileNamesByUserId.get(35L));
		assertEquals(NotificationProfileSourceType.RECIPIENT, sentProfileSourcesByUserId.get(36L));
		assertEquals("Target", sentProfileNamesByUserId.get(36L));
	}

	private Long alertIdOf(Map<Long, NotificationAlert> alerts, NotificationAlertType type) {
		return alerts.entrySet()
			.stream()
			.filter(entry -> entry.getValue().getType() == type)
			.map(Map.Entry::getKey)
			.findFirst()
			.orElseThrow();
	}

	private WorkspaceActivityRecord removedActivity() {
		return WorkspaceActivityRecord.builder()
			.id(921L)
			.workspace(workspace(activeUser(1L)))
			.sourceType(ActivityRecordSourceType.WORKSPACE_MEMBER_ROLE_HISTORY)
			.sourceId(88L)
			.domain(ActivityRecordDomain.WORKSPACE_MEMBER)
			.action(ActivityRecordAction.REMOVED)
			.actorUser(activeUser(34L))
			.actorDisplayNameSnapshot("Actor")
			.targetType(ActivityTargetType.WORKSPACE_MEMBER)
			.targetId(55L)
			.targetDisplayNameSnapshot("Target")
			.changesJson("[]")
			.occurredAt(1782013200L)
			.build();
	}

	private User activeUser(Long id) {
		return User.builder()
			.id(id)
			.email("user%s@example.com".formatted(id))
			.passwordHash("hash")
			.name("user%s".formatted(id))
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}

	private Workspace workspace(User creator) {
		return Workspace.builder()
			.id(12L)
			.name("Flowit")
			.inviteCode("A1B2-C3D4-E5F6")
			.createdBy(creator)
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}
}
