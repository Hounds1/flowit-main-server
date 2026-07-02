package dev.runtime_lab.flowit.domain.task.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.runtime_lab.flowit.domain.task.dto.TaskListQuery;
import dev.runtime_lab.flowit.domain.task.entity.QTask;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskRepositoryTest {

	private final EntityManager entityManager = mock(EntityManager.class);
	private final JPAQueryFactory queryFactory = mock(JPAQueryFactory.class);
	private final TaskRepository repository = new TaskRepository(entityManager, queryFactory);

	@Test
	@SuppressWarnings("unchecked")
	void countActiveByWorkspaceIdUsesAssigneeOrCreatorWhenMineFilterIsRequested() {
		JPAQuery<Long> query = mock(JPAQuery.class);
		TaskListQuery taskQuery = new TaskListQuery(
			null,
			null,
			true,
			null,
			null,
			null,
			null,
			null,
			null
		);
		ArgumentCaptor<Predicate> predicateCaptor = ArgumentCaptor.forClass(Predicate.class);

		when(queryFactory.select(org.mockito.ArgumentMatchers.<Expression<Long>>any())).thenReturn(query);
		when(query.from(QTask.task)).thenReturn(query);
		when(query.where(any(Predicate.class))).thenReturn(query);
		when(query.fetchOne()).thenReturn(1L);

		long count = repository.countActiveByWorkspaceId(1L, taskQuery, null, 10L, 1L);

		assertEquals(1L, count);
		verify(query).where(predicateCaptor.capture());
		String predicate = predicateCaptor.getValue().toString();
		assertTrue(predicate.contains("task.assignee.id = 10"));
		assertTrue(predicate.contains("task.createdBy.id = 1"));
		assertFalse(predicate.contains("task.assignee is null"));
	}
}
