package dev.runtime_lab.flowit.domain.notification.repository;

import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.global.jpa.repository.CustomJpaRepo;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationAlertRepository extends CustomJpaRepo<NotificationAlert, Long> {

	public NotificationAlertRepository(EntityManager entityManager) {
		super(NotificationAlert.class, entityManager);
	}
}
