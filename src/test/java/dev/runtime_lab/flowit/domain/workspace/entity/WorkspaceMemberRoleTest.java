package dev.runtime_lab.flowit.domain.workspace.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceMemberRoleTest {

	@Test
	void authorityUsesWorkspaceScope() {
		assertEquals("WORKSPACE_OWNER", WorkspaceMemberRole.OWNER.authority());
		assertEquals("WORKSPACE_ADMIN", WorkspaceMemberRole.ADMIN.authority());
		assertEquals("WORKSPACE_MEMBER", WorkspaceMemberRole.MEMBER.authority());
	}

	@Test
	void ownerCanUpdateMemberRoleToAnyRole() {
		assertTrue(WorkspaceMemberRole.OWNER.canUpdateMemberRoleTo(WorkspaceMemberRole.OWNER));
		assertTrue(WorkspaceMemberRole.OWNER.canUpdateMemberRoleTo(WorkspaceMemberRole.ADMIN));
		assertTrue(WorkspaceMemberRole.OWNER.canUpdateMemberRoleTo(WorkspaceMemberRole.MEMBER));
	}

	@Test
	void adminCannotUpdateMemberRoleToOwner() {
		assertFalse(WorkspaceMemberRole.ADMIN.canUpdateMemberRoleTo(WorkspaceMemberRole.OWNER));
		assertTrue(WorkspaceMemberRole.ADMIN.canUpdateMemberRoleTo(WorkspaceMemberRole.ADMIN));
		assertTrue(WorkspaceMemberRole.ADMIN.canUpdateMemberRoleTo(WorkspaceMemberRole.MEMBER));
	}

	@Test
	void memberCannotUpdateMemberRole() {
		assertFalse(WorkspaceMemberRole.MEMBER.canUpdateMemberRoleTo(WorkspaceMemberRole.OWNER));
		assertFalse(WorkspaceMemberRole.MEMBER.canUpdateMemberRoleTo(WorkspaceMemberRole.ADMIN));
		assertFalse(WorkspaceMemberRole.MEMBER.canUpdateMemberRoleTo(WorkspaceMemberRole.MEMBER));
	}
}
