package dev.runtime_lab.flowit.domain.workspace.repository;

import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMemberRemovalHistory;
import dev.runtime_lab.flowit.global.jpa.repository.CustomJpaRepo;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceMemberRemovalHistoryRepository extends CustomJpaRepo<WorkspaceMemberRemovalHistory, Long> {

	public WorkspaceMemberRemovalHistoryRepository(EntityManager entityManager) {
		super(WorkspaceMemberRemovalHistory.class, entityManager);
	}
}
