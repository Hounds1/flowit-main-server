package dev.runtime_lab.flowit.domain.activity.dto;

public record ActivityTargetResponse(
	ActivityTargetType type,
	Long id,
	String displayName
) {
}
