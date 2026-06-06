package dev.runtime_lab.flowit.domain.task.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.runtime_lab.flowit.domain.task.entity.QTaskChangeHistory;
import dev.runtime_lab.flowit.domain.task.entity.TaskChangeHistory;
import dev.runtime_lab.flowit.domain.workspace.entity.QWorkspaceMember;
import dev.runtime_lab.flowit.global.jpa.repository.CustomJpaRepo;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TaskChangeHistoryRepository extends CustomJpaRepo<TaskChangeHistory, Long> {

	private final JPAQueryFactory queryFactory;

	public TaskChangeHistoryRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
		super(TaskChangeHistory.class, entityManager);
		this.queryFactory = queryFactory;
	}

	public List<TaskChangeHistory> findByWorkspaceIdAndTaskId(Long workspaceId, Long taskId, int page, int size) {
		QTaskChangeHistory history = QTaskChangeHistory.taskChangeHistory;
		QWorkspaceMember actorMember = new QWorkspaceMember("actorMember");

		return queryFactory.selectFrom(history)
			.join(history.task).fetchJoin()
			.leftJoin(history.actorWorkspaceMember, actorMember).fetchJoin()
			.leftJoin(history.actorUser).fetchJoin()
			.where(
				history.workspace.id.eq(workspaceId),
				history.task.id.eq(taskId)
			)
			.orderBy(history.changedAt.desc(), history.id.desc())
			.offset((long) page * size)
			.limit(size)
			.fetch();
	}

	public long countByWorkspaceIdAndTaskId(Long workspaceId, Long taskId) {
		QTaskChangeHistory history = QTaskChangeHistory.taskChangeHistory;

		Long count = queryFactory.select(history.id.count())
			.from(history)
			.where(
				history.workspace.id.eq(workspaceId),
				history.task.id.eq(taskId)
			)
			.fetchOne();

		return count == null ? 0L : count;
	}

	public List<TaskChangeHistory> findByWorkspaceId(Long workspaceId, int page, int size) {
		QTaskChangeHistory history = QTaskChangeHistory.taskChangeHistory;
		QWorkspaceMember actorMember = new QWorkspaceMember("actorMember");

		return queryFactory.selectFrom(history)
			.join(history.task).fetchJoin()
			.leftJoin(history.actorWorkspaceMember, actorMember).fetchJoin()
			.leftJoin(history.actorUser).fetchJoin()
			.where(history.workspace.id.eq(workspaceId))
			.orderBy(history.changedAt.desc(), history.id.desc())
			.offset((long) page * size)
			.limit(size)
			.fetch();
	}

	public long countByWorkspaceId(Long workspaceId) {
		QTaskChangeHistory history = QTaskChangeHistory.taskChangeHistory;

		Long count = queryFactory.select(history.id.count())
			.from(history)
			.where(history.workspace.id.eq(workspaceId))
			.fetchOne();

		return count == null ? 0L : count;
	}
}
