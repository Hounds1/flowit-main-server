package dev.runtime_lab.flowit.domain.notification.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.runtime_lab.flowit.domain.notification.entity.QNotificationAlert;
import dev.runtime_lab.flowit.domain.notification.entity.QNotificationRecipient;
import dev.runtime_lab.flowit.domain.notification.entity.NotificationRecipient;
import dev.runtime_lab.flowit.global.jpa.repository.CustomJpaRepo;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class NotificationRecipientRepository extends CustomJpaRepo<NotificationRecipient, Long> {

	private final JPAQueryFactory queryFactory;

	public NotificationRecipientRepository(EntityManager entityManager, JPAQueryFactory queryFactory) {
		super(NotificationRecipient.class, entityManager);
		this.queryFactory = queryFactory;
	}

	public List<NotificationRecipient> saveAll(List<NotificationRecipient> recipients) {
		recipients.forEach(this::save);
		return recipients;
	}

	public List<NotificationRecipient> findVisibleByUserId(Long userId, int page, int size) {
		QNotificationRecipient recipient = QNotificationRecipient.notificationRecipient;
		QNotificationAlert alert = QNotificationAlert.notificationAlert;

		return queryFactory.selectFrom(recipient)
			.join(recipient.notificationAlert, alert).fetchJoin()
			.where(
				recipient.userId.eq(userId),
				recipient.hiddenAt.isNull()
			)
			.orderBy(
				recipient.createdAt.desc(),
				alert.groupId.desc().nullsLast(),
				alert.groupSequence.asc(),
				recipient.id.desc()
			)
			.offset((long) page * size)
			.limit(size)
			.fetch();
	}

	public List<NotificationRecipient> findPendingSocketDeliveryByUserId(Long userId, int size) {
		if (size <= 0) {
			return List.of();
		}

		QNotificationRecipient recipient = QNotificationRecipient.notificationRecipient;
		QNotificationAlert alert = QNotificationAlert.notificationAlert;

		return queryFactory.selectFrom(recipient)
			.join(recipient.notificationAlert, alert).fetchJoin()
			.where(
				recipient.userId.eq(userId),
				recipient.hiddenAt.isNull(),
				recipient.socketSentAt.isNull()
			)
			.orderBy(
				recipient.createdAt.asc(),
				alert.groupId.asc().nullsLast(),
				alert.groupSequence.asc(),
				recipient.id.asc()
			)
			.limit(size)
			.fetch();
	}

	public long countVisibleByUserId(Long userId) {
		QNotificationRecipient recipient = QNotificationRecipient.notificationRecipient;

		Long count = queryFactory.select(recipient.id.count())
			.from(recipient)
			.where(
				recipient.userId.eq(userId),
				recipient.hiddenAt.isNull()
			)
			.fetchOne();

		return count == null ? 0L : count;
	}

	public long countVisibleUnreadByUserId(Long userId) {
		QNotificationRecipient recipient = QNotificationRecipient.notificationRecipient;

		Long count = queryFactory.select(recipient.id.count())
			.from(recipient)
			.where(
				recipient.userId.eq(userId),
				recipient.hiddenAt.isNull(),
				recipient.readAt.isNull()
			)
			.fetchOne();

		return count == null ? 0L : count;
	}

	public long countVisibleUnseenByUserId(Long userId) {
		QNotificationRecipient recipient = QNotificationRecipient.notificationRecipient;

		Long count = queryFactory.select(recipient.id.count())
			.from(recipient)
			.where(
				recipient.userId.eq(userId),
				recipient.hiddenAt.isNull(),
				recipient.seenAt.isNull()
			)
			.fetchOne();

		return count == null ? 0L : count;
	}

	@Transactional
	public int markVisibleUnseenAsSeenByUserId(Long userId, Long seenAt) {
		QNotificationRecipient recipient = QNotificationRecipient.notificationRecipient;

		return Math.toIntExact(queryFactory.update(recipient)
			.set(recipient.seenAt, seenAt)
			.where(
				recipient.userId.eq(userId),
				recipient.hiddenAt.isNull(),
				recipient.seenAt.isNull()
			)
			.execute());
	}

	@Transactional
	public int markVisibleUnreadAsReadByUserId(Long userId, Long readAt) {
		QNotificationRecipient recipient = QNotificationRecipient.notificationRecipient;

		return Math.toIntExact(queryFactory.update(recipient)
			.set(recipient.readAt, readAt)
			.where(
				recipient.userId.eq(userId),
				recipient.hiddenAt.isNull(),
				recipient.readAt.isNull()
			)
			.execute());
	}

	@Transactional
	public int markSocketSentIfPending(Long recipientId, Long socketSentAt) {
		QNotificationRecipient recipient = QNotificationRecipient.notificationRecipient;

		return Math.toIntExact(queryFactory.update(recipient)
			.set(recipient.socketSentAt, socketSentAt)
			.where(
				recipient.id.eq(recipientId),
				recipient.hiddenAt.isNull(),
				recipient.socketSentAt.isNull()
			)
			.execute());
	}
}
