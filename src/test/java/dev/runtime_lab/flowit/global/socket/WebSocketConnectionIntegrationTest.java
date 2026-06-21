package dev.runtime_lab.flowit.global.socket;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.global.security.cors.CorsProperties;
import dev.runtime_lab.flowit.global.security.jwt.interceptor.StompJwtChannelInterceptor;
import dev.runtime_lab.flowit.global.socket.config.WebSocketConfig;
import dev.runtime_lab.flowit.global.socket.config.WebSocketSecurityConfig;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	classes = WebSocketConnectionIntegrationTest.TestApplication.class
)
class WebSocketConnectionIntegrationTest {

	private static final String USER_NOTIFICATION_DESTINATION = "/user/queue/notifications";
	private static final Logger log = LoggerFactory.getLogger(WebSocketConnectionIntegrationTest.class);

	@LocalServerPort
	private int port;

	@Autowired
	private WebSocketPublisher webSocketPublisher;

	@Autowired
	private SimpUserRegistry simpUserRegistry;

	@Test
	void authenticatedClientsReceivePerspectiveSpecificNotifications() throws Exception {
		WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
		stompClient.setMessageConverter(new JacksonJsonMessageConverter());
		String url = "ws://localhost:%d/ws".formatted(port);

		SocketClient removedMemberClient = null;
		SocketClient workspaceMemberClient = null;

		try {
			removedMemberClient = connectAndSubscribe(stompClient, url, 7L, "removed-member");
			workspaceMemberClient = connectAndSubscribe(stompClient, url, 8L, "workspace-member");

			NotificationAlertResponse accessRevokedNotification = notification(
				100L,
				NotificationAlertType.WORKSPACE_ACCESS_REVOKED,
				NotificationLinkType.NONE,
				null
			);
			NotificationAlertResponse memberRemovedNotification = notification(
				101L,
				NotificationAlertType.WORKSPACE_MEMBER_REMOVED,
				NotificationLinkType.WORKSPACE_MEMBERS,
				12L
			);

			publish(removedMemberClient, accessRevokedNotification);
			publish(workspaceMemberClient, memberRemovedNotification);

			NotificationAlertResponse removedMemberReceived = receive(removedMemberClient);
			NotificationAlertResponse workspaceMemberReceived = receive(workspaceMemberClient);

			assertEquals(100L, removedMemberReceived.id());
			assertEquals(NotificationAlertType.WORKSPACE_ACCESS_REVOKED, removedMemberReceived.type());
			assertEquals(101L, workspaceMemberReceived.id());
			assertEquals(NotificationAlertType.WORKSPACE_MEMBER_REMOVED, workspaceMemberReceived.type());
		}
		finally {
			disconnect(removedMemberClient);
			disconnect(workspaceMemberClient);
			stompClient.stop();
		}
	}

	private SocketClient connectAndSubscribe(
		WebSocketStompClient stompClient,
		String url,
		Long userId,
		String perspective
	) throws Exception {
		StompHeaders connectHeaders = new StompHeaders();
		connectHeaders.add("Authorization", bearerToken(userId));
		WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();

		log.info("[websocket-test] connect perspective={} user={} url={} authorizationHeader=true",
			perspective,
			userId,
			url
		);
		StompSession session = stompClient
			.connectAsync(
				url,
				handshakeHeaders,
				connectHeaders,
				new StompSessionHandlerAdapter() {
			})
			.get(3, TimeUnit.SECONDS);
		log.info("[websocket-test] connected perspective={} user={} sessionId={}",
			perspective,
			userId,
			session.getSessionId()
		);

		BlockingQueue<NotificationAlertResponse> messages = new LinkedBlockingQueue<>();
		log.info("[websocket-test] subscribe perspective={} user={} destination={}",
			perspective,
			userId,
			USER_NOTIFICATION_DESTINATION
		);
		session.subscribe(USER_NOTIFICATION_DESTINATION, new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return NotificationAlertResponse.class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				messages.add((NotificationAlertResponse) payload);
			}
		});
		assertTrue(
			awaitUserNotificationSubscription(userId),
			"subscription must be registered for user " + userId
		);
		log.info("[websocket-test] subscription registered perspective={} user={} destination={}",
			perspective,
			userId,
			USER_NOTIFICATION_DESTINATION
		);
		return new SocketClient(userId, perspective, session, messages);
	}

	private void publish(SocketClient client, NotificationAlertResponse notification) throws Exception {
		log.info("[websocket-test] publish perspective={} userId={} alertId={} type={}",
			client.perspective(),
			client.userId(),
			notification.id(),
			notification.type()
		);
		log.info("[websocket-test] publish payload={}", toJson(notification));
		webSocketPublisher.publishUserNotification(client.userId(), notification);
	}

	private NotificationAlertResponse receive(SocketClient client) throws Exception {
		NotificationAlertResponse received = client.messages().poll(3, TimeUnit.SECONDS);
		assertNotNull(received);
		log.info("[websocket-test] received perspective={} userId={} alertId={} type={} read={}",
			client.perspective(),
			client.userId(),
			received.id(),
			received.type(),
			received.read()
		);
		log.info("[websocket-test] received payload={}", toJson(received));
		return received;
	}

	private void disconnect(SocketClient client) {
		if (client == null) {
			return;
		}
		log.info("[websocket-test] disconnect perspective={} user={} sessionId={}",
			client.perspective(),
			client.userId(),
			client.session().getSessionId()
		);
		client.session().disconnect();
	}

	private boolean awaitUserNotificationSubscription(Long userId) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
		while (System.nanoTime() < deadline) {
			if (hasUserNotificationSubscription(userId)) {
				return true;
			}
			Thread.sleep(50);
		}
		return false;
	}

	private boolean hasUserNotificationSubscription(Long userId) {
		var user = simpUserRegistry.getUser(String.valueOf(userId));
		return user != null && user.getSessions()
			.stream()
			.flatMap(session -> session.getSubscriptions().stream())
			.anyMatch(subscription -> USER_NOTIFICATION_DESTINATION.equals(subscription.getDestination()));
	}

	private String toJson(NotificationAlertResponse response) throws Exception {
		return JsonMapper.builder()
			.build()
			.writeValueAsString(response);
	}

	private NotificationAlertResponse notification(
		Long id,
		NotificationAlertType type,
		NotificationLinkType linkType,
		Long linkWorkspaceId
	) {
		return new NotificationAlertResponse(
			id,
			type,
			1782013200L,
			new NotificationScopeResponse(NotificationScopeType.WORKSPACE, 12L, "Flowit"),
			new NotificationActorResponse(NotificationActorType.USER, 34L, "Actor", null),
			new NotificationSubjectResponse(NotificationSubjectType.WORKSPACE_MEMBER, 55L, "Target"),
			List.of(),
			new NotificationLinkResponse(linkType, linkWorkspaceId),
			false
		);
	}

	private String bearerToken(Long userId) {
		return "Bearer test-user-%d-token".formatted(userId);
	}

	private record SocketClient(
		Long userId,
		String perspective,
		StompSession session,
		BlockingQueue<NotificationAlertResponse> messages
	) {
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration(excludeName = {
		"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
		"org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
		"org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration",
		"org.springframework.boot.data.redis.autoconfigure.RedisAutoConfiguration",
		"org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration"
	})
	@Import({
		WebSocketConfig.class,
		WebSocketSecurityConfig.class,
		StompJwtChannelInterceptor.class,
		WebSocketPublisher.class
	})
	static class TestApplication {

		@Bean
		CorsProperties corsProperties() {
			return new CorsProperties(
				List.of("http://localhost"),
				null,
				null,
				null,
				null,
				null
			);
		}

		@Bean
		JwtDecoder jwtDecoder() {
			Instant now = Instant.now();
			return token -> Jwt.withTokenValue(token)
				.header("alg", "none")
				.subject(token.replace("test-user-", "").replace("-token", ""))
				.claim("email", "%s@example.com".formatted(token))
				.claim("name", token)
				.issuedAt(now)
				.expiresAt(now.plusSeconds(60))
				.build();
		}

		@Bean
		SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
			return http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
				.build();
		}
	}
}
