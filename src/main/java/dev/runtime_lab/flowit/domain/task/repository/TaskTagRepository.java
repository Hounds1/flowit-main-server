package dev.runtime_lab.flowit.domain.task.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.runtime_lab.flowit.domain.task.entity.QTaskTag;
import dev.runtime_lab.flowit.domain.task.entity.TaskTag;
import dev.runtime_lab.flowit.global.jpa.repository.CustomJpaRepo;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TaskTagRepository extends CustomJpaRepo<TaskTag, Long> {

	private final JPAQueryFactory queryFactory;

	public TaskTagRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
		super(TaskTag.class, entityManager);
		this.queryFactory = queryFactory;
	}

	public List<TaskTag> findByTaskId(Long taskId) {
		QTaskTag taskTag = QTaskTag.taskTag;

		return queryFactory.selectFrom(taskTag)
			.join(taskTag.task).fetchJoin()
			.where(taskTag.task.id.eq(taskId))
			.orderBy(taskTag.sortOrder.asc(), taskTag.id.asc())
			.fetch();
	}

	public List<TaskTag> findByTaskIds(List<Long> taskIds) {
		if (taskIds.isEmpty()) {
			return List.of();
		}

		QTaskTag taskTag = QTaskTag.taskTag;

		return queryFactory.selectFrom(taskTag)
			.join(taskTag.task).fetchJoin()
			.where(taskTag.task.id.in(taskIds))
			.orderBy(taskTag.task.id.asc(), taskTag.sortOrder.asc(), taskTag.id.asc())
			.fetch();
	}

	public long deleteByTaskId(Long taskId) {
		QTaskTag taskTag = QTaskTag.taskTag;

		return queryFactory.delete(taskTag)
			.where(taskTag.task.id.eq(taskId))
			.execute();
	}
}
