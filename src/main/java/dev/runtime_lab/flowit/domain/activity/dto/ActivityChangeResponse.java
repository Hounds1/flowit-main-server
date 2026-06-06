package dev.runtime_lab.flowit.domain.activity.dto;

public record ActivityChangeResponse(
	ActivityChangeElement element,
	Object from,
	Object to
) {
}
