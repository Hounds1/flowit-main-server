package dev.runtime_lab.flowit.domain.activity.repository;

import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;
import dev.runtime_lab.flowit.global.jpa.repository.CustomJpaRepo;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class WorkspaceActivityRecordRepository extends CustomJpaRepo<WorkspaceActivityRecord, Long> {

	public WorkspaceActivityRecordRepository(EntityManager entityManager) {
		super(WorkspaceActivityRecord.class, entityManager);
	}

	public List<WorkspaceActivityRecord> findByWorkspaceId(
		Long workspaceId,
		ActivityRecordDomain domain,
		Long occurredFrom
	) {
		return entityManager().createQuery("""
				select record
				from WorkspaceActivityRecord record
				left join fetch record.actorWorkspaceMember
				left join fetch record.actorUser
				where record.workspace.id = :workspaceId
					and (:domain is null or record.domain = :domain)
					and record.occurredAt >= :occurredFrom
				order by record.occurredAt desc, record.id desc
				""", WorkspaceActivityRecord.class)
			.setParameter("workspaceId", workspaceId)
			.setParameter("domain", domain)
			.setParameter("occurredFrom", occurredFrom)
			.getResultList();
	}
}
