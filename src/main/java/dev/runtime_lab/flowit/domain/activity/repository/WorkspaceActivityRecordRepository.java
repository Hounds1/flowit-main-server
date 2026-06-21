package dev.runtime_lab.flowit.domain.activity.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.entity.QWorkspaceActivityRecord;
import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;
import dev.runtime_lab.flowit.domain.user.entity.QUser;
import dev.runtime_lab.flowit.domain.workspace.entity.QWorkspace;
import dev.runtime_lab.flowit.domain.workspace.entity.QWorkspaceMember;
import dev.runtime_lab.flowit.global.jpa.repository.CustomJpaRepo;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class WorkspaceActivityRecordRepository extends CustomJpaRepo<WorkspaceActivityRecord, Long> {

	private final JPAQueryFactory queryFactory;

	public WorkspaceActivityRecordRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
		super(WorkspaceActivityRecord.class, entityManager);
		this.queryFactory = queryFactory;
	}

	public List<WorkspaceActivityRecord> findByWorkspaceId(
		Long workspaceId,
		ActivityRecordDomain domain,
		Long occurredFrom
	) {
		QWorkspaceActivityRecord record = QWorkspaceActivityRecord.workspaceActivityRecord;
		QWorkspaceMember actorWorkspaceMember = new QWorkspaceMember("actorWorkspaceMember");
		QUser actorUser = new QUser("actorUser");

		BooleanBuilder condition = new BooleanBuilder()
				.and(record.workspace.id.eq(workspaceId))
				.and(record.occurredAt.goe(occurredFrom));

		if (domain != null) {
			condition.and(record.domain.eq(domain));
		}

		return queryFactory.selectFrom(record)
				.leftJoin(record.actorWorkspaceMember, actorWorkspaceMember).fetchJoin()
				.leftJoin(record.actorUser, actorUser).fetchJoin()
				.where(condition)
				.orderBy(record.occurredAt.desc(), record.id.desc())
				.fetch();
	}

	public Optional<WorkspaceActivityRecord> findByIdWithWorkspaceAndActor(Long id) {
		QWorkspaceActivityRecord record = QWorkspaceActivityRecord.workspaceActivityRecord;
		QWorkspace workspace = QWorkspace.workspace;
		QWorkspaceMember actorWorkspaceMember = new QWorkspaceMember("actorWorkspaceMember");
		QUser actorUser = new QUser("actorUser");

		return Optional.ofNullable(queryFactory.selectFrom(record)
			.join(record.workspace, workspace).fetchJoin()
			.leftJoin(record.actorWorkspaceMember, actorWorkspaceMember).fetchJoin()
			.leftJoin(record.actorUser, actorUser).fetchJoin()
			.where(record.id.eq(id))
			.fetchOne());
	}
}
