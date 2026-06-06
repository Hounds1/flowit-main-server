package dev.runtime_lab.flowit.domain.activity.dto;

public enum ActivityRecordTopic {
	ALL,
	TASK,
	MEMBER;

	public ActivityRecordDomain domain() {
		return switch (this) {
			case ALL -> null;
			case TASK -> ActivityRecordDomain.TASK;
			case MEMBER -> ActivityRecordDomain.WORKSPACE_MEMBER;
		};
	}
}
