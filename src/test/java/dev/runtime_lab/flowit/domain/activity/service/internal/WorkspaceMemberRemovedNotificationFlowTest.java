package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordAction;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityTargetType;
import dev.runtime_lab.flowit.domain.activity.entity.ActivityRecordSourceType;
import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationRecipient;
import dev.runtime_lab.flowit.domain.notification.event.NotificationAlertCreatedEvent;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationAlertRepository;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationRecipientRepository;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertCreateService;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertResponseAssembler;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertSocketDispatchLoader;
import dev.runtime_lab.flowit.domain.notification.service.internal.NotificationAlertSocketEventListener;
import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.workspace.entity.Workspace;
import dev.runtime_lab.flowit.domain.workspace.service.internal.WorkspaceMembershipQueryService;
import dev.runtime_lab.flowit.global.socket.WebSocketPublisher;
import dev.runtime_lab.flowit.global.socket.dto.WebSocketPayload;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
	private final WebSocketPublisher webSocketPublisher = mock(WebSocketPublisher.class);
	private final Clock clock = Clock.fixed(Instant.ofEpochSecond(1782013300L), ZoneOffset.UTC);
	private final NotificationAlertResponseAssembler responseAssembler =
		new NotificationAlertResponseAssembler(JsonMapper.builder().build());
	private final NotificationAlertCreateService createService = new NotificationAlertCreateService(
		notificationAlertRepository,
		notificationRecipientRepository,
		workspaceMembershipQueryService,
		eventPublisher,
		clock
	);
	private final NotificationAlertSocketDispatchLoader socketDispatchLoader = new NotificationAlertSocketDispatchLoader(
		notificationAlertRepository,
		notificationRecipientRepository,
		responseAssembler
	);
	private final NotificationAlertSocketEventListener socketEventListener = new NotificationAlertSocketEventListener(
		socketDispatchLoader,
		webSocketPublisher
	);

	@Test
	void removedMemberActivityCreatesPerspectiveSpecificAlertsAndPublishesDifferentPayloads() {
		Map<Long, NotificationAlert> savedAlerts = new LinkedHashMap<>();
		Map<Long, List<Long>> recipientUserIdsByAlertId = new LinkedHashMap<>();
		List<NotificationAlertCreatedEvent> createdEvents = new ArrayList<>();
		AtomicLong alertIdSequence = new AtomicLong(100L);

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
		when(notificationRecipientRepository.findVisibleUserIdsByNotificationAlertId(anyLong()))
			.thenAnswer(invocation -> recipientUserIdsByAlertId.getOrDefault(invocation.getArgument(0), List.of()));
		when(workspaceMembershipQueryService.findActiveMemberUserIds(12L)).thenReturn(List.of(34L, 35L));
		when(workspaceMembershipQueryService.findMemberUserId(12L, 55L)).thenReturn(Optional.of(36L));
		doAnswer(invocation -> {
			createdEvents.add(invocation.getArgument(0));
			return null;
		}).when(eventPublisher).publishEvent(any(Object.class));

		commandFactory.create(removedActivity()).forEach(createService::create);
		createdEvents.forEach(socketEventListener::publishNotification);

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
		for (int index = 0; index < userIdCaptor.getAllValues().size(); index++) {
			NotificationAlertResponse response = (NotificationAlertResponse) payloadCaptor.getAllValues().get(index);
			sentTypesByUserId.put(userIdCaptor.getAllValues().get(index), response.type());
		}

		assertEquals(NotificationAlertType.WORKSPACE_MEMBER_REMOVED, sentTypesByUserId.get(34L));
		assertEquals(NotificationAlertType.WORKSPACE_MEMBER_REMOVED, sentTypesByUserId.get(35L));
		assertEquals(NotificationAlertType.WORKSPACE_ACCESS_REVOKED, sentTypesByUserId.get(36L));
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
