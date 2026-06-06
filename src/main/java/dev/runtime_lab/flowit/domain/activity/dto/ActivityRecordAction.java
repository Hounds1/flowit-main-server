package dev.runtime_lab.flowit.domain.activity.dto;

import dev.runtime_lab.flowit.domain.task.entity.TaskHistoryAction;

public enum ActivityRecordAction {
	CREATED,
	MODIFIED,
	STATUS_CHANGED,
	PROGRESS_CHANGED,
	ROLE_CHANGED,
	REMOVED,
	WITHDRAWN,
	JOINED;

	public static ActivityRecordAction from(TaskHistoryAction action) {
		return switch (action) {
			case CREATED -> CREATED;
			case MODIFIED -> MODIFIED;
			case STATUS_CHANGED -> STATUS_CHANGED;
			case PROGRESS_CHANGED -> PROGRESS_CHANGED;
		};
	}
}
