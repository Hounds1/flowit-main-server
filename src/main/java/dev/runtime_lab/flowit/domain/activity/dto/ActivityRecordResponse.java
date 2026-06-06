package dev.runtime_lab.flowit.domain.activity.dto;

import java.util.List;

public record ActivityRecordResponse(
	Long id,
	Long occurredAt,
	ActivityRecordDomain domain,
	ActivityActorResponse actor,
	ActivityTargetResponse target,
	ActivityRecordAction action,
	Integer changeCount,
	List<ActivityChangeElement> changedElements,
	List<ActivityChangeResponse> changes
) {
}
