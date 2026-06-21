package dev.runtime_lab.flowit.domain.activity.event;

import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordAction;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;

public record WorkspaceActivityRecordedEvent(
	Long activityRecordId,
	Long workspaceId,
	ActivityRecordDomain domain,
	ActivityRecordAction action,
	Long occurredAt
) {

	public static WorkspaceActivityRecordedEvent from(WorkspaceActivityRecord record) {
		return new WorkspaceActivityRecordedEvent(
			record.getId(),
			record.getWorkspace().getId(),
			record.getDomain(),
			record.getAction(),
			record.getOccurredAt()
		);
	}
}
