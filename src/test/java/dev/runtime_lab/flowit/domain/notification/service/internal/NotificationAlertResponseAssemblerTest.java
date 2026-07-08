package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileSourceType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationSourceType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationAlertResponseAssemblerTest {

	private final NotificationProfileResolver notificationProfileResolver = mock(NotificationProfileResolver.class);
	private final NotificationAlertResponseAssembler assembler =
		new NotificationAlertResponseAssembler(JsonMapper.builder().build(), notificationProfileResolver);

	@Test
	void assemblesNotificationAlertResponse() {
		NotificationAlert alert = NotificationAlert.builder()
			.id(1L)
			.sourceType(NotificationSourceType.WORKSPACE_ACTIVITY_RECORD)
			.sourceId(921L)
			.type(NotificationAlertType.WORKSPACE_MEMBER_ROLE_CHANGED)
			.scopeType(NotificationScopeType.WORKSPACE)
			.scopeId(12L)
			.scopeNameSnapshot("Flowit")
			.actorType(NotificationActorType.USER)
			.actorId(34L)
			.actorNameSnapshot("Actor")
			.subjectType(NotificationSubjectType.WORKSPACE_MEMBER)
			.subjectId(55L)
			.subjectNameSnapshot("Target")
			.changesJson("""
				[{"element":"ROLE","from":"MEMBER","to":"ADMIN"}]
				""")
			.linkType(NotificationLinkType.WORKSPACE_MEMBERS)
			.linkWorkspaceId(12L)
			.occurredAt(1782013200L)
			.createdAt(1782013300L)
			.build();
		when(notificationProfileResolver.resolve(alert, 7L))
			.thenReturn(new NotificationProfileResponse(
				NotificationProfileSourceType.SUBJECT,
				"/v1/workspaces/12/members/55/profile-image"
			));

		NotificationAlertResponse response = assembler.toResponse(alert, 7L, false);

		assertEquals(1L, response.id());
		assertEquals(NotificationAlertType.WORKSPACE_MEMBER_ROLE_CHANGED, response.type());
		assertEquals(NotificationProfileSourceType.SUBJECT, response.profile().source());
		assertEquals("/v1/workspaces/12/members/55/profile-image", response.profile().profileImageUrl());
		assertEquals(12L, response.scope().id());
		assertEquals("Flowit", response.scope().name());
		assertEquals(34L, response.actor().id());
		assertEquals("Actor", response.actor().name());
		assertEquals(55L, response.subject().id());
		assertEquals("Target", response.subject().name());
		assertEquals("ROLE", response.changes().get(0).element());
		assertEquals("MEMBER", response.changes().get(0).from());
		assertEquals("ADMIN", response.changes().get(0).to());
		assertFalse(response.read());
	}
}
