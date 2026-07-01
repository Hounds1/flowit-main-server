package dev.runtime_lab.flowit.domain.activity.service.internal;

import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordAction;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityTargetType;
import dev.runtime_lab.flowit.domain.activity.entity.ActivityRecordSourceType;
import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationActorType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationLinkType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationScopeType;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationSubjectType;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationSourceType;
import dev.runtime_lab.flowit.domain.notification.service.internal.command.NotificationAlertCreateCommand;
import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.workspace.entity.Workspace;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceMemberActivityNotificationCommandFactoryTest {

	private final WorkspaceMemberActivityNotificationCommandFactory factory =
		new WorkspaceMemberActivityNotificationCommandFactory();

	@Test
	void createsNotificationCommandFromWorkspaceMemberActivity() {
		WorkspaceActivityRecord record = WorkspaceActivityRecord.builder()
			.id(921L)
			.workspace(workspace(activeUser(1L)))
			.sourceType(ActivityRecordSourceType.WORKSPACE_MEMBER_ROLE_HISTORY)
			.sourceId(88L)
			.domain(ActivityRecordDomain.WORKSPACE_MEMBER)
			.action(ActivityRecordAction.ROLE_CHANGED)
			.actorUser(activeUser(34L))
			.actorDisplayNameSnapshot("Actor")
			.targetType(ActivityTargetType.WORKSPACE_MEMBER)
			.targetId(55L)
			.targetDisplayNameSnapshot("Target")
			.changesJson("""
				[{"element":"ROLE","from":"MEMBER","to":"ADMIN"}]
				""")
			.occurredAt(1782013200L)
			.build();

		List<NotificationAlertCreateCommand> response = factory.create(record);

		assertEquals(1, response.size());
		NotificationAlertCreateCommand command = response.get(0);
		assertEquals(NotificationSourceType.WORKSPACE_ACTIVITY_RECORD, command.sourceType());
		assertEquals(921L, command.sourceId());
		assertEquals(NotificationAlertType.WORKSPACE_MEMBER_ROLE_CHANGED, command.type());
		assertEquals(NotificationScopeType.WORKSPACE, command.scopeType());
		assertEquals(12L, command.scopeId());
		assertEquals("Flowit", command.scopeName());
		assertEquals(NotificationActorType.USER, command.actorType());
		assertEquals(34L, command.actorId());
		assertEquals("Actor", command.actorName());
		assertEquals(NotificationSubjectType.WORKSPACE_MEMBER, command.subjectType());
		assertEquals(55L, command.subjectId());
		assertEquals("Target", command.subjectName());
		assertEquals(NotificationLinkType.WORKSPACE_MEMBERS, command.linkType());
		assertEquals(12L, command.linkWorkspaceId());
		assertEquals(1782013200L, command.occurredAt());
		assertEquals("WORKSPACE_ACTIVITY_RECORD:921", command.groupId());
		assertEquals(10, command.groupSequence());
	}

	@Test
	void createsMemberAndRemovedUserNotificationCommandsFromRemovedActivity() {
		WorkspaceActivityRecord record = WorkspaceActivityRecord.builder()
			.id(921L)
			.workspace(workspace(activeUser(1L)))
			.sourceType(ActivityRecordSourceType.WORKSPACE_MEMBER_ROLE_HISTORY)
			.sourceId(88L)
			.domain(ActivityRecordDomain.WORKSPACE_MEMBER)
			.action(ActivityRecordAction.REMOVED)
			.actorUser(activeUser(34L))
			.actorDisplayNameSnapshot("Actor")
			.targetType(ActivityTargetType.WORKSPACE_MEMBER)
			.targetId(55L)
			.targetDisplayNameSnapshot("Target")
			.changesJson("[]")
			.occurredAt(1782013200L)
			.build();

		List<NotificationAlertCreateCommand> response = factory.create(record);

		assertEquals(2, response.size());

		NotificationAlertCreateCommand memberCommand = response.get(0);
		assertEquals(NotificationAlertType.WORKSPACE_MEMBER_REMOVED, memberCommand.type());
		assertEquals(NotificationLinkType.WORKSPACE_MEMBERS, memberCommand.linkType());
		assertEquals(12L, memberCommand.linkWorkspaceId());
		assertEquals("WORKSPACE_ACTIVITY_RECORD:921", memberCommand.groupId());
		assertEquals(10, memberCommand.groupSequence());

		NotificationAlertCreateCommand removedUserCommand = response.get(1);
		assertEquals(NotificationAlertType.WORKSPACE_ACCESS_REVOKED, removedUserCommand.type());
		assertEquals(NotificationLinkType.NONE, removedUserCommand.linkType());
		assertEquals(null, removedUserCommand.linkWorkspaceId());
		assertEquals(55L, removedUserCommand.subjectId());
		assertEquals("Target", removedUserCommand.subjectName());
		assertEquals("WORKSPACE_ACTIVITY_RECORD:921", removedUserCommand.groupId());
		assertEquals(20, removedUserCommand.groupSequence());
	}

	@Test
	void ignoresOtherActivityDomains() {
		WorkspaceActivityRecord record = WorkspaceActivityRecord.builder()
			.domain(ActivityRecordDomain.TASK)
			.action(ActivityRecordAction.CREATED)
			.build();

		assertTrue(factory.create(record).isEmpty());
	}

	private User activeUser(Long id) {
		return User.builder()
			.id(id)
			.email("user%s@example.com".formatted(id))
			.passwordHash("hash")
			.name("user%s".formatted(id))
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}

	private Workspace workspace(User creator) {
		return Workspace.builder()
			.id(12L)
			.name("Flowit")
			.inviteCode("A1B2-C3D4-E5F6")
			.createdBy(creator)
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}
}
