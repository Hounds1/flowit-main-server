package dev.runtime_lab.flowit.domain.workspace.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.runtime_lab.flowit.domain.user.entity.User;
import dev.runtime_lab.flowit.domain.workspace.entity.QWorkspaceMember;
import dev.runtime_lab.flowit.domain.workspace.entity.Workspace;
import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMember;
import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMemberRole;
import dev.runtime_lab.flowit.global.jpa.repository.CustomJpaRepo;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceMemberRepository extends CustomJpaRepo<WorkspaceMember, Long> {

	private final JPAQueryFactory queryFactory;

	public WorkspaceMemberRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
		super(WorkspaceMember.class, entityManager);
		this.queryFactory = queryFactory;
	}

	public boolean existsActiveOwnerByWorkspaceAndUser(Workspace workspace, User user) {
		QWorkspaceMember workspaceMember = QWorkspaceMember.workspaceMember;

		return queryFactory.selectOne()
			.from(workspaceMember)
			.where(
				workspaceMember.workspace.eq(workspace),
				workspaceMember.user.eq(user),
				workspaceMember.role.eq(WorkspaceMemberRole.OWNER),
				workspaceMember.deletedAt.isNull()
			)
			.fetchFirst() != null;
	}

	public long softDeleteActiveByWorkspaceId(Long workspaceId, Long deletedAt) {
		QWorkspaceMember workspaceMember = QWorkspaceMember.workspaceMember;

		return queryFactory.update(workspaceMember)
			.set(workspaceMember.updatedAt, deletedAt)
			.set(workspaceMember.deletedAt, deletedAt)
			.where(
				workspaceMember.workspace.id.eq(workspaceId),
				workspaceMember.deletedAt.isNull()
			)
			.execute();
	}
}
