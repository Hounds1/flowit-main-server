package dev.runtime_lab.flowit.domain.workspace.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.workspace.entity.QWorkspaceMember;
import dev.runtime_lab.flowit.domain.workspace.entity.Workspace;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkspaceMemberRepositoryTest {

	private final EntityManager entityManager = mock(EntityManager.class);
	private final JPAQueryFactory queryFactory = mock(JPAQueryFactory.class);
	private final WorkspaceMemberRepository repository = new WorkspaceMemberRepository(entityManager, queryFactory);

	@Test
	@SuppressWarnings("unchecked")
	void existsActiveOwnerByWorkspaceAndUserReturnsTrueWhenFound() {
		JPAQuery<Integer> query = mock(JPAQuery.class);
		User user = activeUser();
		Workspace workspace = workspace(user);

		when(queryFactory.selectOne()).thenReturn(query);
		when(query.from(QWorkspaceMember.workspaceMember)).thenReturn(query);
		when(query.where(
			any(Predicate.class),
			any(Predicate.class),
			any(Predicate.class),
			any(Predicate.class)
		)).thenReturn(query);
		when(query.fetchFirst()).thenReturn(1);

		boolean exists = repository.existsActiveOwnerByWorkspaceAndUser(workspace, user);

		assertTrue(exists);
		verify(queryFactory).selectOne();
		verify(query).from(QWorkspaceMember.workspaceMember);
		verify(query).where(
			any(Predicate.class),
			any(Predicate.class),
			any(Predicate.class),
			any(Predicate.class)
		);
		verify(query).fetchFirst();
	}

	@Test
	@SuppressWarnings("unchecked")
	void softDeleteActiveByWorkspaceIdUpdatesActiveRows() {
		JPAUpdateClause update = mock(JPAUpdateClause.class);

		when(queryFactory.update(QWorkspaceMember.workspaceMember)).thenReturn(update);
		when(update.set(QWorkspaceMember.workspaceMember.updatedAt, 1779889000L)).thenReturn(update);
		when(update.set(QWorkspaceMember.workspaceMember.deletedAt, 1779889000L)).thenReturn(update);
		when(update.where(any(Predicate.class), any(Predicate.class))).thenReturn(update);
		when(update.execute()).thenReturn(3L);

		long updatedCount = repository.softDeleteActiveByWorkspaceId(10L, 1779889000L);

		assertEquals(3L, updatedCount);
		verify(queryFactory).update(QWorkspaceMember.workspaceMember);
		verify(update).set(QWorkspaceMember.workspaceMember.updatedAt, 1779889000L);
		verify(update).set(QWorkspaceMember.workspaceMember.deletedAt, 1779889000L);
		verify(update).where(any(Predicate.class), any(Predicate.class));
		verify(update).execute();
	}

	private User activeUser() {
		return User.builder()
			.id(1L)
			.email("user@example.com")
			.passwordHash("hash")
			.name("nickname")
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}

	private Workspace workspace(User creator) {
		return Workspace.builder()
			.id(10L)
			.name("Flowit")
			.inviteCode("A1B2-C3D4-E5F6")
			.createdBy(creator)
			.createdAt(1L)
			.updatedAt(1L)
			.build();
	}
}
