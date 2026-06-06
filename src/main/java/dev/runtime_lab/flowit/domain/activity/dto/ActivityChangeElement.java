package dev.runtime_lab.flowit.domain.activity.dto;

import dev.runtime_lab.flowit.domain.task.entity.TaskHistoryElement;

public enum ActivityChangeElement {
	TITLE,
	DESCRIPTION,
	STATUS,
	ASSIGNEE,
	PRIORITY,
	START_DATE,
	DUE_DATE,
	TAGS,
	PROGRESS,
	ROLE,
	MEMBERSHIP,
	OWNERSHIP_TRANSFER;

	public static ActivityChangeElement from(TaskHistoryElement element) {
		return switch (element) {
			case TITLE -> TITLE;
			case DESCRIPTION -> DESCRIPTION;
			case STATUS -> STATUS;
			case ASSIGNEE -> ASSIGNEE;
			case PRIORITY -> PRIORITY;
			case START_DATE -> START_DATE;
			case DUE_DATE -> DUE_DATE;
			case TAGS -> TAGS;
			case PROGRESS -> PROGRESS;
		};
	}
}
