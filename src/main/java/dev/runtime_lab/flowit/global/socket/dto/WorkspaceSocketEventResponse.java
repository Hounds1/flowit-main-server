package dev.runtime_lab.flowit.global.socket.dto;

public record WorkspaceSocketEventResponse<T extends WebSocketPayload>(
	String type,
	Long workspaceId,
	Long occurredAt,
	T payload
) implements WebSocketPayload {
}
