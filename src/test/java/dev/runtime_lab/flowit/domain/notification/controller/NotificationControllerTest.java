package dev.runtime_lab.flowit.domain.notification.controller;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertListResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertReadAllResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertSeenResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationProfileSourceType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.service.NotificationService;
import dev.runtime_lab.flowit.global.security.authentication.AuthenticatedUserArgumentResolver;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import dev.runtime_lab.flowit.global.web.exception.GlobalExceptionHandler;
import dev.runtime_lab.flowit.global.web.response.ApiResponseBodyAdvice;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

	private final NotificationService notificationService = mock(NotificationService.class);
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
			.standaloneSetup(new NotificationController(notificationService))
			.setCustomArgumentResolvers(new AuthenticatedUserArgumentResolver())
			.setControllerAdvice(new ApiResponseBodyAdvice(), new GlobalExceptionHandler())
			.build();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void alertsReturnCurrentUserNotifications() throws Exception {
		ArgumentCaptor<CurrentUser> currentUserCaptor = ArgumentCaptor.forClass(CurrentUser.class);

		when(notificationService.alerts(any(CurrentUser.class), eq(1), eq(20)))
			.thenReturn(new NotificationAlertListResponse(List.of(alert(false)), 10L, 3L, 2L));
		SecurityContextHolder.getContext().setAuthentication(
			new JwtAuthenticationToken(jwt("7", "user@example.com", "User"), List.of())
		);

		mockMvc.perform(get("/v1/notifications")
				.param("page", "1")
				.param("size", "20")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.items[0].id").value(1L))
			.andExpect(jsonPath("$.data.items[0].type").value("WORKSPACE_MEMBER_JOINED"))
			.andExpect(jsonPath("$.data.items[0].scope.type").value("WORKSPACE"))
			.andExpect(jsonPath("$.data.items[0].scope.id").value(12L))
			.andExpect(jsonPath("$.data.items[0].profile.source").value("SUBJECT"))
			.andExpect(jsonPath("$.data.items[0].profile.profileImageUrl").value("/v1/workspaces/12/members/55/profile-image"))
			.andExpect(jsonPath("$.data.items[0].actor.name").value("Actor"))
			.andExpect(jsonPath("$.data.items[0].subject.name").value("Target"))
			.andExpect(jsonPath("$.data.items[0].link.type").value("WORKSPACE_MEMBERS"))
			.andExpect(jsonPath("$.data.items[0].read").value(false))
			.andExpect(jsonPath("$.data.totalCount").value(10L))
			.andExpect(jsonPath("$.data.unreadCount").value(3L))
			.andExpect(jsonPath("$.data.unseenCount").value(2L))
			.andExpect(jsonPath("$.extensions").isMap());

		verify(notificationService).alerts(currentUserCaptor.capture(), eq(1), eq(20));
		assertEquals(7L, currentUserCaptor.getValue().id());
	}

	@Test
	void seenMarksCurrentUserNotificationsAsSeen() throws Exception {
		ArgumentCaptor<CurrentUser> currentUserCaptor = ArgumentCaptor.forClass(CurrentUser.class);

		when(notificationService.seen(any(CurrentUser.class)))
			.thenReturn(new NotificationAlertSeenResponse(1782013300L, 2L));
		SecurityContextHolder.getContext().setAuthentication(
			new JwtAuthenticationToken(jwt("7", "user@example.com", "User"), List.of())
		);

		mockMvc.perform(patch("/v1/notifications/seen")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.seenAt").value(1782013300L))
			.andExpect(jsonPath("$.data.seenCount").value(2L))
			.andExpect(jsonPath("$.extensions").isMap());

		verify(notificationService).seen(currentUserCaptor.capture());
		assertEquals(7L, currentUserCaptor.getValue().id());
	}

	@Test
	void readAllMarksCurrentUserNotificationsAsRead() throws Exception {
		ArgumentCaptor<CurrentUser> currentUserCaptor = ArgumentCaptor.forClass(CurrentUser.class);

		when(notificationService.readAll(any(CurrentUser.class)))
			.thenReturn(new NotificationAlertReadAllResponse(1782013300L, 3L));
		SecurityContextHolder.getContext().setAuthentication(
			new JwtAuthenticationToken(jwt("7", "user@example.com", "User"), List.of())
		);

		mockMvc.perform(patch("/v1/notifications/read-all")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.readAt").value(1782013300L))
			.andExpect(jsonPath("$.data.readCount").value(3L))
			.andExpect(jsonPath("$.extensions").isMap());

		verify(notificationService).readAll(currentUserCaptor.capture());
		assertEquals(7L, currentUserCaptor.getValue().id());
	}

	@Test
	void alertsReturnUnauthorizedWhenAuthenticationIsMissing() throws Exception {
		mockMvc.perform(get("/v1/notifications")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.error.code").value("AUTH_401_001"))
			.andExpect(jsonPath("$.extensions").isMap());
	}

	private NotificationAlertResponse alert(boolean read) {
		return new NotificationAlertResponse(
			1L,
			NotificationAlertType.WORKSPACE_MEMBER_JOINED,
			1782013200L,
			new NotificationProfileResponse(
				NotificationProfileSourceType.SUBJECT,
				"/v1/workspaces/12/members/55/profile-image"
			),
			new NotificationScopeResponse(NotificationScopeType.WORKSPACE, 12L, "Flowit"),
			new NotificationActorResponse(NotificationActorType.USER, 34L, "Actor"),
			new NotificationSubjectResponse(NotificationSubjectType.WORKSPACE_MEMBER, 55L, "Target"),
			List.of(),
			new NotificationLinkResponse(NotificationLinkType.WORKSPACE_MEMBERS, 12L),
			read
		);
	}

	private Jwt jwt(String subject, String email, String name) {
		return Jwt.withTokenValue("token")
			.header("alg", "none")
			.subject(subject)
			.claim("email", email)
			.claim("name", name)
			.issuedAt(Instant.now())
			.expiresAt(Instant.now().plusSeconds(60))
			.build();
	}
}
