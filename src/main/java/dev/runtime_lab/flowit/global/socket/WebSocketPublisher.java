package dev.runtime_lab.flowit.global.socket;

import dev.runtime_lab.flowit.global.socket.dto.WebSocketPayload;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketPublisher {

	private static final String USER_NOTIFICATIONS_QUEUE_DESTINATION = "/queue/notifications";

	private final SimpMessagingTemplate template;

	public void publishUserNotification(Long userId, WebSocketPayload payload) {
		template.convertAndSendToUser(
			String.valueOf(Objects.requireNonNull(userId, "userId must not be null")),
			USER_NOTIFICATIONS_QUEUE_DESTINATION,
			Objects.requireNonNull(payload, "payload must not be null")
		);
	}
}
