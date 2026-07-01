package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordAction;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityTargetType;
import dev.runtime_lab.flowit.domain.activity.entity.ActivityRecordSourceType;
import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.service.internal.command.NotificationAlertCreateCommand;
import dev.runtime_lab.flowit.domain.task.entity.Task;
import dev.runtime_lab.flowit.domain.task.entity.TaskPriority;
import dev.runtime_lab.flowit.domain.task.entity.TaskStatus;
import dev.runtime_lab.flowit.domain.task.service.internal.TaskNotificationTargetQueryService;
import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.workspace.entity.Workspace;
import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMember;
import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMemberRole;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskActivityNotificationCommandFactoryTest {

	private final TaskNotificationTargetQueryService taskNotificationTargetQueryService =
		mock(TaskNotificationTargetQueryService.class);
	private final TaskActivityNotificationCommandFactory factory =
		new TaskActivityNotificationCommandFactory(taskNotificationTargetQueryService, JsonMapper.builder().build());

	@Test
	void createsTaskCreatedThenAssignedNotificationsForInitialAssignee() {
		User actor = user(34L, "Actor");
		User assigneeUser = user(35L, "Assignee");
		Workspace workspace = workspace();
		Task task = task(workspace, actor, workspaceMember(55L, workspace, assigneeUser));
		WorkspaceActivityRecord record = taskActivity(
			workspace,
			actor,
			ActivityRecordAction.CREATED,
			initialAssigneeChangesJson(55L, 35L, "Assignee")
		);

		when(taskNotificationTargetQueryService.findActiveTask(12L, 1001L)).thenReturn(Optional.of(task));

		List<NotificationAlertCreateCommand> commands = factory.create(record);

		assertEquals(2, commands.size());
		assertEquals(NotificationAlertType.TASK_CREATED, commands.get(0).type());
		assertEquals(List.of(35L), commands.get(0).recipientUserIds());
		assertEquals("WORKSPACE_ACTIVITY_RECORD:921", commands.get(0).groupId());
		assertEquals(10, commands.get(0).groupSequence());
		assertEquals("[]", commands.get(0).changesJson());
		assertEquals(NotificationAlertType.TASK_ASSIGNED, commands.get(1).type());
		assertEquals(List.of(35L), commands.get(1).recipientUserIds());
		assertEquals("WORKSPACE_ACTIVITY_RECORD:921", commands.get(1).groupId());
		assertEquals(30, commands.get(1).groupSequence());
		assertTrue(commands.get(1).changesJson().contains("ASSIGNEE"));
	}

	@Test
	void skipsInitialAssigneeNotificationsWhenCreatorAssignedToSelf() {
		User actor = user(34L, "Actor");
		Workspace workspace = workspace();
		Task task = task(workspace, actor, workspaceMember(55L, workspace, actor));
		WorkspaceActivityRecord record = taskActivity(
			workspace,
			actor,
			ActivityRecordAction.CREATED,
			initialAssigneeChangesJson(55L, 34L, "Actor")
		);

		when(taskNotificationTargetQueryService.findActiveTask(12L, 1001L)).thenReturn(Optional.of(task));

		assertTrue(factory.create(record).isEmpty());
	}

	@Test
	void createsGeneralNotificationForCreatorAndCurrentAssigneeExceptActor() {
		User actor = user(34L, "Actor");
		User assigneeUser = user(35L, "Assignee");
		Workspace workspace = workspace();
		Task task = task(workspace, actor, workspaceMember(55L, workspace, assigneeUser));
		WorkspaceActivityRecord record = taskActivity(
			workspace,
			actor,
			ActivityRecordAction.STATUS_CHANGED,
			"""
				[{"element":"STATUS","from":"TODO","to":"IN_PROGRESS"}]
				"""
		);

		when(taskNotificationTargetQueryService.findActiveTask(12L, 1001L)).thenReturn(Optional.of(task));

		List<NotificationAlertCreateCommand> commands = factory.create(record);

		assertEquals(1, commands.size());
		NotificationAlertCreateCommand command = commands.get(0);
		assertEquals(NotificationAlertType.TASK_STATUS_CHANGED, command.type());
		assertEquals(NotificationSubjectType.TASK, command.subjectType());
		assertEquals(1001L, command.subjectId());
		assertEquals(NotificationLinkType.TASK_DETAIL, command.linkType());
		assertEquals(12L, command.linkWorkspaceId());
		assertEquals(List.of(35L), command.recipientUserIds());
		assertEquals("WORKSPACE_ACTIVITY_RECORD:921", command.groupId());
		assertEquals(50, command.groupSequence());
		assertTrue(command.changesJson().contains("STATUS"));
	}

	@Test
	void createsAssigneeChangedNotificationsWithoutDuplicatingCurrentAssigneeCreator() {
		User actor = user(34L, "Actor");
		User creatorAndAssignee = user(35L, "Creator");
		Workspace workspace = workspace();
		Task task = task(workspace, creatorAndAssignee, workspaceMember(56L, workspace, creatorAndAssignee));
		WorkspaceActivityRecord record = taskActivity(
			workspace,
			actor,
			ActivityRecordAction.MODIFIED,
			assigneeChangesJson(55L, 36L, "Previous", 56L, 35L, "Creator")
		);

		when(taskNotificationTargetQueryService.findActiveTask(12L, 1001L)).thenReturn(Optional.of(task));

		List<NotificationAlertCreateCommand> commands = factory.create(record);

		assertEquals(2, commands.size());
		assertEquals(NotificationAlertType.TASK_UNASSIGNED, commands.get(0).type());
		assertEquals(List.of(36L), commands.get(0).recipientUserIds());
		assertEquals(20, commands.get(0).groupSequence());
		assertTrue(commands.get(0).changesJson().contains("ASSIGNEE"));
		assertEquals(NotificationAlertType.TASK_ASSIGNED, commands.get(1).type());
		assertEquals(List.of(35L), commands.get(1).recipientUserIds());
		assertEquals(30, commands.get(1).groupSequence());
		assertTrue(commands.get(1).changesJson().contains("ASSIGNEE"));
	}

	@Test
	void createsDateNotificationForCreatorWhenAssigneeAndDateChangedToAnotherUser() {
		User actor = user(34L, "Actor");
		User creator = user(37L, "Creator");
		User currentAssignee = user(35L, "Assignee");
		Workspace workspace = workspace();
		Task task = task(workspace, creator, workspaceMember(56L, workspace, currentAssignee));
		WorkspaceActivityRecord record = taskActivity(
			workspace,
			actor,
			ActivityRecordAction.MODIFIED,
			assigneeAndDateChangesJson(55L, 36L, "Previous", 56L, 35L, "Assignee")
		);

		when(taskNotificationTargetQueryService.findActiveTask(12L, 1001L)).thenReturn(Optional.of(task));

		List<NotificationAlertCreateCommand> commands = factory.create(record);

		assertEquals(3, commands.size());
		assertEquals(NotificationAlertType.TASK_UNASSIGNED, commands.get(0).type());
		assertEquals(List.of(36L), commands.get(0).recipientUserIds());
		assertTrue(commands.get(0).changesJson().contains("ASSIGNEE"));
		assertTrue(!commands.get(0).changesJson().contains("DUE_DATE"));
		assertEquals(NotificationAlertType.TASK_ASSIGNED, commands.get(1).type());
		assertEquals(List.of(35L), commands.get(1).recipientUserIds());
		assertTrue(commands.get(1).changesJson().contains("ASSIGNEE"));
		assertTrue(!commands.get(1).changesJson().contains("DUE_DATE"));
		assertEquals(NotificationAlertType.TASK_DATE_CHANGED, commands.get(2).type());
		assertEquals(List.of(37L), commands.get(2).recipientUserIds());
		assertTrue(commands.get(2).changesJson().contains("DUE_DATE"));
		assertTrue(!commands.get(2).changesJson().contains("ASSIGNEE"));
		assertEquals(List.of(20, 30, 40), commands.stream()
			.map(NotificationAlertCreateCommand::groupSequence)
			.toList());
	}

	@Test
	void createsDateThenStatusNotificationsWithSeparatedChangesWhenStatusAndDateChanged() {
		User actor = user(34L, "Actor");
		User creator = user(37L, "Creator");
		User currentAssignee = user(35L, "Assignee");
		Workspace workspace = workspace();
		Task task = task(workspace, creator, workspaceMember(56L, workspace, currentAssignee));
		WorkspaceActivityRecord record = taskActivity(
			workspace,
			actor,
			ActivityRecordAction.STATUS_CHANGED,
			statusAndDateChangesJson()
		);

		when(taskNotificationTargetQueryService.findActiveTask(12L, 1001L)).thenReturn(Optional.of(task));

		List<NotificationAlertCreateCommand> commands = factory.create(record);

		assertEquals(2, commands.size());
		assertEquals(NotificationAlertType.TASK_DATE_CHANGED, commands.get(0).type());
		assertEquals(40, commands.get(0).groupSequence());
		assertTrue(commands.get(0).changesJson().contains("DUE_DATE"));
		assertTrue(!commands.get(0).changesJson().contains("STATUS"));
		assertEquals(NotificationAlertType.TASK_STATUS_CHANGED, commands.get(1).type());
		assertEquals(50, commands.get(1).groupSequence());
		assertTrue(commands.get(1).changesJson().contains("STATUS"));
		assertTrue(!commands.get(1).changesJson().contains("DUE_DATE"));
	}

	@Test
	void skipsGeneralModifiedNotificationWhenNoDateChanged() {
		User actor = user(34L, "Actor");
		User creator = user(37L, "Creator");
		User currentAssignee = user(35L, "Assignee");
		Workspace workspace = workspace();
		Task task = task(workspace, creator, workspaceMember(56L, workspace, currentAssignee));
		WorkspaceActivityRecord record = taskActivity(
			workspace,
			actor,
			ActivityRecordAction.MODIFIED,
			"""
				[{"element":"TITLE","from":"Login UI","to":"Login screen UI"}]
				"""
		);

		when(taskNotificationTargetQueryService.findActiveTask(12L, 1001L)).thenReturn(Optional.of(task));

		assertTrue(factory.create(record).isEmpty());
	}

	@Test
	void ignoresOtherActivityDomains() {
		WorkspaceActivityRecord record = WorkspaceActivityRecord.builder()
			.domain(ActivityRecordDomain.WORKSPACE_MEMBER)
			.build();

		assertTrue(factory.create(record).isEmpty());
	}

	private WorkspaceActivityRecord taskActivity(
		Workspace workspace,
		User actor,
		ActivityRecordAction action,
		String changesJson
	) {
		return WorkspaceActivityRecord.builder()
			.id(921L)
			.workspace(workspace)
			.sourceType(ActivityRecordSourceType.TASK_CHANGE_HISTORY)
			.sourceId(88L)
			.domain(ActivityRecordDomain.TASK)
			.action(action)
			.actorUser(actor)
			.actorDisplayNameSnapshot(actor.getName())
			.targetType(ActivityTargetType.TASK)
			.targetId(1001L)
			.targetDisplayNameSnapshot("Login UI")
			.changesJson(changesJson)
			.occurredAt(1782013200L)
			.build();
	}

	private String assigneeChangesJson(
		Long fromMemberId,
		Long fromUserId,
		String fromDisplayName,
		Long toMemberId,
		Long toUserId,
		String toDisplayName
	) {
		return """
			[
			  {
			    "element": "ASSIGNEE",
			    "from": {"memberId": %d, "userId": %d, "displayName": "%s"},
			    "to": {"memberId": %d, "userId": %d, "displayName": "%s"}
			  }
			]
			""".formatted(fromMemberId, fromUserId, fromDisplayName, toMemberId, toUserId, toDisplayName);
	}

	private String initialAssigneeChangesJson(
		Long toMemberId,
		Long toUserId,
		String toDisplayName
	) {
		return """
			[
			  {
			    "element": "ASSIGNEE",
			    "from": null,
			    "to": {"memberId": %d, "userId": %d, "displayName": "%s"}
			  }
			]
			""".formatted(toMemberId, toUserId, toDisplayName);
	}

	private String assigneeAndDateChangesJson(
		Long fromMemberId,
		Long fromUserId,
		String fromDisplayName,
		Long toMemberId,
		Long toUserId,
		String toDisplayName
	) {
		return """
			[
			  {
			    "element": "ASSIGNEE",
			    "from": {"memberId": %d, "userId": %d, "displayName": "%s"},
			    "to": {"memberId": %d, "userId": %d, "displayName": "%s"}
			  },
			  {
			    "element": "DUE_DATE",
			    "from": 1781222400,
			    "to": 1781827200
			  }
			]
			""".formatted(fromMemberId, fromUserId, fromDisplayName, toMemberId, toUserId, toDisplayName);
	}

	private String statusAndDateChangesJson() {
		return """
			[
			  {
			    "element": "STATUS",
			    "from": "TODO",
			    "to": "IN_PROGRESS"
			  },
			  {
			    "element": "DUE_DATE",
			    "from": 1781222400,
			    "to": 1781827200
			  }
			]
			""";
	}

	private Task task(Workspace workspace, User creator, WorkspaceMember assignee) {
		return Task.builder()
			.id(1001L)
			.workspace(workspace)
			.title("Login UI")
			.descriptionMarkdown("Build login UI")
			.status(TaskStatus.TODO)
			.priority(TaskPriority.HIGH)
			.assignee(assignee)
			.createdBy(creator)
			.createdAt(1782013100L)
			.updatedAt(1782013200L)
			.build();
	}

	private WorkspaceMember workspaceMember(Long id, Workspace workspace, User user) {
		return WorkspaceMember.builder()
			.id(id)
			.workspace(workspace)
			.user(user)
			.role(WorkspaceMemberRole.MEMBER)
			.joinedAt(1L)
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}

	private Workspace workspace() {
		return Workspace.builder()
			.id(12L)
			.name("Flowit")
			.inviteCode("A1B2-C3D4-E5F6")
			.createdBy(user(1L, "Owner"))
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}

	private User user(Long id, String name) {
		return User.builder()
			.id(id)
			.email("user%s@example.com".formatted(id))
			.passwordHash("hash")
			.name(name)
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}
}
