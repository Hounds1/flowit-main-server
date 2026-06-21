package dev.runtime_lab.flowit.global.outbox.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.runtime_lab.flowit.global.jpa.repository.CustomJpaRepo;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEvent;
import dev.runtime_lab.flowit.global.outbox.entity.OutboxEventStatus;
import dev.runtime_lab.flowit.global.outbox.entity.QOutboxEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OutboxEventRepository extends CustomJpaRepo<OutboxEvent, Long> {

	private final JPAQueryFactory queryFactory;

	public OutboxEventRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
		super(OutboxEvent.class, entityManager);
		this.queryFactory = queryFactory;
	}

	public Optional<OutboxEvent> findPendingByIdForUpdate(Long id) {
		QOutboxEvent event = QOutboxEvent.outboxEvent;

		return Optional.ofNullable(queryFactory.selectFrom(event)
			.where(
				event.id.eq(id),
				event.status.eq(OutboxEventStatus.PENDING)
			)
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetchOne());
	}
}
