package dev.runtime_lab.flowit.domain.workspace.repository;

import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMemberRoleHistory;
import dev.runtime_lab.flowit.global.jpa.repository.CustomJpaRepo;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceMemberRoleHistoryRepository extends CustomJpaRepo<WorkspaceMemberRoleHistory, Long> {

	public WorkspaceMemberRoleHistoryRepository(EntityManager entityManager) {
		super(WorkspaceMemberRoleHistory.class, entityManager);
	}
}
