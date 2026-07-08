package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.dto.ActivityChangeElement;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityChangeResponse;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordAction;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationSourceType;
import dev.runtime_lab.flowit.domain.notification.service.internal.command.NotificationAlertCreateCommand;
import dev.runtime_lab.flowit.domain.task.entity.Task;
import dev.runtime_lab.flowit.domain.task.service.internal.TaskNotificationTargetQueryService;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@InternalService
@RequiredArgsConstructor
public class TaskActivityNotificationCommandFactory implements WorkspaceActivityNotificationCommandFactory {

	private static final int TASK_CREATED_SEQUENCE = 10;
	private static final int TASK_UNASSIGNED_SEQUENCE = 20;
	private static final int TASK_ASSIGNED_SEQUENCE = 30;
	private static final int TASK_DATE_CHANGED_SEQUENCE = 40;
	private static final int TASK_STATUS_CHANGED_SEQUENCE = 50;
	private static final int TASK_PROGRESS_CHANGED_SEQUENCE = 60;

	private final TaskNotificationTargetQueryService taskNotificationTargetQueryService;
	private final JsonMapper jsonMapper;

	@Override
	public List<NotificationAlertCreateCommand> create(WorkspaceActivityRecord record) {
		if (record.getDomain() != ActivityRecordDomain.TASK) {
			return List.of();
		}

		return taskNotificationTargetQueryService.findActiveTask(record.getWorkspace().getId(), record.getTargetId())
			.map(task -> create(record, task))
			.orElseGet(List::of);
	}

	private List<NotificationAlertCreateCommand> create(WorkspaceActivityRecord record, Task task) {
		List<ActivityChangeResponse> changes = changes(record.getChangesJson());
		Optional<ActivityChangeResponse> assigneeChange = assigneeChange(changes);
		Long actorUserId = record.getActorUser() == null ? null : record.getActorUser().getId();
		List<NotificationAlertCreateCommand> commands = new ArrayList<>();
		Set<Long> directlyNotifiedUserIds = new LinkedHashSet<>();

		if (record.getAction() == ActivityRecordAction.CREATED) {
			List<Long> initialAssigneeRecipientUserIds = initialAssigneeRecipientUserIds(task, actorUserId);
			if (!initialAssigneeRecipientUserIds.isEmpty()) {
				commands.add(command(
					record,
					NotificationAlertType.TASK_CREATED,
					writeChanges(taskCreatedChanges(changes)),
					initialAssigneeRecipientUserIds,
					TASK_CREATED_SEQUENCE
				));
				assigneeChange.ifPresent(change -> commands.add(command(
						record,
						NotificationAlertType.TASK_ASSIGNED,
						writeChanges(List.of(change)),
						initialAssigneeRecipientUserIds,
						TASK_ASSIGNED_SEQUENCE
					)));
			}
			return commands;
		}

		if (assigneeChange.isPresent()) {
			Long previousAssigneeUserId = memberUserId(assigneeChange.get().from());
			Long currentAssigneeUserId = memberUserId(assigneeChange.get().to());
			addDirectCommand(
				commands,
				directlyNotifiedUserIds,
				record,
				assigneeChange.get(),
				NotificationAlertType.TASK_UNASSIGNED,
				previousAssigneeUserId,
				actorUserId
			);
			addDirectCommand(
				commands,
				directlyNotifiedUserIds,
				record,
				assigneeChange.get(),
				NotificationAlertType.TASK_ASSIGNED,
				currentAssigneeUserId,
				actorUserId
			);
		}

		List<Long> generalRecipientUserIds = generalRecipientUserIds(task, actorUserId, directlyNotifiedUserIds);
		if (!generalRecipientUserIds.isEmpty()) {
			dateChanges(changes).ifPresent(dateChanges -> commands.add(command(
					record,
					NotificationAlertType.TASK_DATE_CHANGED,
					writeChanges(dateChanges),
					generalRecipientUserIds,
					TASK_DATE_CHANGED_SEQUENCE
				)));
			generalType(record.getAction())
				.flatMap(type -> alertChanges(type, changes)
					.map(alertChanges -> command(
						record,
						type,
						writeChanges(alertChanges),
						generalRecipientUserIds,
						groupSequence(type)
					)))
				.ifPresent(commands::add);
		}

		return commands;
	}

	private void addDirectCommand(
		List<NotificationAlertCreateCommand> commands,
		Set<Long> directlyNotifiedUserIds,
		WorkspaceActivityRecord record,
		ActivityChangeResponse assigneeChange,
		NotificationAlertType type,
		Long recipientUserId,
		Long actorUserId
	) {
		if (recipientUserId == null || Objects.equals(recipientUserId, actorUserId)) {
			return;
		}

		commands.add(command(
			record,
			type,
			writeChanges(List.of(assigneeChange)),
			List.of(recipientUserId),
			groupSequence(type)
		));
		directlyNotifiedUserIds.add(recipientUserId);
	}

	private List<Long> initialAssigneeRecipientUserIds(Task task, Long actorUserId) {
		LinkedHashSet<Long> recipientUserIds = new LinkedHashSet<>();
		if (task.getAssignee() != null) {
			recipientUserIds.add(task.getAssignee().getUser().getId());
		}

		recipientUserIds.remove(null);
		recipientUserIds.remove(task.getCreatedBy().getId());
		recipientUserIds.remove(actorUserId);
		return List.copyOf(recipientUserIds);
	}

	private List<Long> generalRecipientUserIds(Task task, Long actorUserId, Set<Long> directlyNotifiedUserIds) {
		LinkedHashSet<Long> recipientUserIds = new LinkedHashSet<>();
		recipientUserIds.add(task.getCreatedBy().getId());
		if (task.getAssignee() != null) {
			recipientUserIds.add(task.getAssignee().getUser().getId());
		}

		recipientUserIds.remove(null);
		recipientUserIds.remove(actorUserId);
		recipientUserIds.removeAll(directlyNotifiedUserIds);
		return List.copyOf(recipientUserIds);
	}

	private NotificationAlertCreateCommand command(
		WorkspaceActivityRecord record,
		NotificationAlertType type,
		String changesJson,
		List<Long> recipientUserIds,
		Integer groupSequence
	) {
		return new NotificationAlertCreateCommand(
			NotificationSourceType.WORKSPACE_ACTIVITY_RECORD,
			record.getId(),
			type,
			NotificationScopeType.WORKSPACE,
			record.getWorkspace().getId(),
			record.getWorkspace().getName(),
			record.getActorUser() == null ? null : NotificationActorType.USER,
			record.getActorUser() == null ? null : record.getActorUser().getId(),
			record.getActorDisplayNameSnapshot(),
			NotificationSubjectType.TASK,
			record.getTargetId(),
			record.getTargetDisplayNameSnapshot(),
			changesJson,
			NotificationLinkType.TASK_DETAIL,
			record.getWorkspace().getId(),
			record.getOccurredAt(),
			recipientUserIds,
			notificationGroupId(record),
			groupSequence
		);
	}

	private String notificationGroupId(WorkspaceActivityRecord record) {
		return "%s:%d".formatted(NotificationSourceType.WORKSPACE_ACTIVITY_RECORD.name(), record.getId());
	}

	private Optional<ActivityChangeResponse> assigneeChange(List<ActivityChangeResponse> changes) {
		return changes.stream()
			.filter(change -> change.element() == ActivityChangeElement.ASSIGNEE)
			.findFirst();
	}

	private Optional<List<ActivityChangeResponse>> dateChanges(List<ActivityChangeResponse> changes) {
		List<ActivityChangeResponse> dateChanges = changes.stream()
			.filter(change -> change.element() == ActivityChangeElement.START_DATE
				|| change.element() == ActivityChangeElement.DUE_DATE)
			.toList();

		return dateChanges.isEmpty() ? Optional.empty() : Optional.of(dateChanges);
	}

	private List<ActivityChangeResponse> taskCreatedChanges(List<ActivityChangeResponse> changes) {
		return changes.stream()
			.filter(change -> change.element() != ActivityChangeElement.ASSIGNEE)
			.toList();
	}

	private Optional<List<ActivityChangeResponse>> alertChanges(
		NotificationAlertType type,
		List<ActivityChangeResponse> changes
	) {
		List<ActivityChangeElement> elements = switch (type) {
			case TASK_STATUS_CHANGED -> List.of(ActivityChangeElement.STATUS);
			case TASK_PROGRESS_CHANGED -> List.of(ActivityChangeElement.PROGRESS);
			default -> List.of();
		};
		if (elements.isEmpty()) {
			return Optional.empty();
		}

		List<ActivityChangeResponse> alertChanges = changes.stream()
			.filter(change -> elements.contains(change.element()))
			.toList();

		return alertChanges.isEmpty() ? Optional.empty() : Optional.of(alertChanges);
	}

	private Optional<NotificationAlertType> generalType(ActivityRecordAction action) {
		return switch (action) {
			case CREATED -> Optional.of(NotificationAlertType.TASK_CREATED);
			case STATUS_CHANGED -> Optional.of(NotificationAlertType.TASK_STATUS_CHANGED);
			case PROGRESS_CHANGED -> Optional.of(NotificationAlertType.TASK_PROGRESS_CHANGED);
			default -> Optional.empty();
		};
	}

	private int groupSequence(NotificationAlertType type) {
		return switch (type) {
			case TASK_CREATED -> TASK_CREATED_SEQUENCE;
			case TASK_UNASSIGNED -> TASK_UNASSIGNED_SEQUENCE;
			case TASK_ASSIGNED -> TASK_ASSIGNED_SEQUENCE;
			case TASK_DATE_CHANGED -> TASK_DATE_CHANGED_SEQUENCE;
			case TASK_STATUS_CHANGED -> TASK_STATUS_CHANGED_SEQUENCE;
			case TASK_PROGRESS_CHANGED -> TASK_PROGRESS_CHANGED_SEQUENCE;
			default -> 0;
		};
	}

	private Long memberUserId(Object value) {
		if (!(value instanceof Map<?, ?> memberValue)) {
			return null;
		}

		Object userId = memberValue.get("userId");
		if (userId instanceof Number number) {
			return number.longValue();
		}
		if (userId instanceof String text) {
			return Long.parseLong(text);
		}
		return null;
	}

	private List<ActivityChangeResponse> changes(String changesJson) {
		try {
			return jsonMapper.readValue(changesJson, new TypeReference<>() {
			});
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Failed to deserialize task activity changes.", exception);
		}
	}

	private String writeChanges(List<ActivityChangeResponse> changes) {
		try {
			return jsonMapper.writeValueAsString(changes);
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Failed to serialize task activity notification changes.", exception);
		}
	}
}
