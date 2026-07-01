package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;
import dev.runtime_lab.flowit.domain.notification.service.internal.command.NotificationAlertCreateCommand;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.util.List;

@InternalService
public interface WorkspaceActivityNotificationCommandFactory {

	List<NotificationAlertCreateCommand> create(WorkspaceActivityRecord record);
}
