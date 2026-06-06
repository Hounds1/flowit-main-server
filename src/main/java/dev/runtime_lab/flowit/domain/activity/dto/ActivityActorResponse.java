package dev.runtime_lab.flowit.domain.activity.dto;

public record ActivityActorResponse(
	Long memberId,
	Long userId,
	String displayName
) {
}
