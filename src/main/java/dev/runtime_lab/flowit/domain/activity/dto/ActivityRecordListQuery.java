package dev.runtime_lab.flowit.domain.activity.dto;

public record ActivityRecordListQuery(
	ActivityRecordTopic topic,
	Integer rangeDays
) {
	private static final int DEFAULT_RANGE_DAYS = 5;

	public ActivityRecordTopic topicOrDefault() {
		return topic == null ? ActivityRecordTopic.ALL : topic;
	}

	public int rangeDaysOrDefault() {
		return rangeDays == null ? DEFAULT_RANGE_DAYS : rangeDays;
	}
}
