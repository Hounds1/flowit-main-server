package dev.runtime_lab.flowit.domain.notification.service.internal;

import dev.runtime_lab.flowit.domain.notification.entity.NotificationAlert;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationAlertRepository;
import dev.runtime_lab.flowit.domain.notification.repository.NotificationRecipientRepository;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@InternalService
@RequiredArgsConstructor
public class NotificationAlertSocketDispatchLoader {

	private final NotificationAlertRepository notificationAlertRepository;
	private final NotificationRecipientRepository notificationRecipientRepository;
	private final NotificationAlertResponseAssembler notificationAlertResponseAssembler;

	@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
	public Optional<NotificationAlertSocketDispatch> load(Long notificationAlertId) {
		return notificationAlertRepository.findById(notificationAlertId)
			.map(this::toDispatch);
	}

	private NotificationAlertSocketDispatch toDispatch(NotificationAlert notificationAlert) {
		return new NotificationAlertSocketDispatch(
			notificationAlertResponseAssembler.toResponse(notificationAlert, false),
			notificationRecipientRepository.findVisibleUserIdsByNotificationAlertId(notificationAlert.getId())
		);
	}
}
