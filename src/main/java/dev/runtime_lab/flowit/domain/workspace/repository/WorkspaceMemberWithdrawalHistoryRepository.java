package dev.runtime_lab.flowit.domain.workspace.repository;

import dev.runtime_lab.flowit.domain.workspace.entity.WorkspaceMemberWithdrawalHistory;
import dev.runtime_lab.flowit.global.jpa.repository.CustomJpaRepo;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceMemberWithdrawalHistoryRepository extends CustomJpaRepo<WorkspaceMemberWithdrawalHistory, Long> {

	public WorkspaceMemberWithdrawalHistoryRepository(EntityManager entityManager) {
		super(WorkspaceMemberWithdrawalHistory.class, entityManager);
	}
}
