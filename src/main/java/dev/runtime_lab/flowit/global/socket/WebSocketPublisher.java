package dev.runtime_lab.flowit.global.socket;

import dev.runtime_lab.flowit.global.socket.dto.WebSocketPayload;
import dev.runtime_lab.flowit.global.socket.dto.WorkspaceSocketEventResponse;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketPublisher {

	public static final String USER_NOTIFICATIONS_SUBSCRIPTION_DESTINATION = "/user/queue/notifications";

	private static final String WORKSPACE_EVENTS_DESTINATION_PREFIX = "/topic/workspaces/";
	private static final String WORKSPACE_EVENTS_DESTINATION_SUFFIX = "/events";
	private static final String USER_NOTIFICATIONS_QUEUE_DESTINATION = "/queue/notifications";

	private final SimpMessagingTemplate template;

	public void publishWorkspaceEvent(WorkspaceSocketEventResponse<?> event) {
		Objects.requireNonNull(event, "event must not be null");

		template.convertAndSend(workspaceEventsDestination(event.workspaceId()), event);
	}

	public void publishUserNotification(Long userId, WebSocketPayload payload) {
		template.convertAndSendToUser(
			String.valueOf(Objects.requireNonNull(userId, "userId must not be null")),
			USER_NOTIFICATIONS_QUEUE_DESTINATION,
			Objects.requireNonNull(payload, "payload must not be null")
		);
	}

	public static String workspaceEventsDestination(Long workspaceId) {
		return WORKSPACE_EVENTS_DESTINATION_PREFIX
			+ Objects.requireNonNull(workspaceId, "workspaceId must not be null")
			+ WORKSPACE_EVENTS_DESTINATION_SUFFIX;
	}
}
