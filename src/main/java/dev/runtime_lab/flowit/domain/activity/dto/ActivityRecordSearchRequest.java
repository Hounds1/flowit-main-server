package dev.runtime_lab.flowit.domain.activity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ActivityRecordSearchRequest(
	ActivityRecordTopic topic,

	@Min(1)
	@Max(365)
	Integer rangeDays
) {

	public ActivityRecordListQuery toQuery() {
		return new ActivityRecordListQuery(topic, rangeDays);
	}
}
