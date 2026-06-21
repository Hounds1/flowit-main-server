package dev.runtime_lab.flowit.domain.notification.controller;

import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertListResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertReadAllResponse;
import dev.runtime_lab.flowit.domain.notification.dto.NotificationAlertSeenResponse;
import dev.runtime_lab.flowit.domain.notification.service.NotificationService;
import dev.runtime_lab.flowit.global.security.authentication.AuthenticatedUser;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public NotificationAlertListResponse alerts(
		@AuthenticatedUser CurrentUser currentUser,
		@RequestParam(required = false) Integer page,
		@RequestParam(required = false) Integer size
	) {
		return notificationService.alerts(currentUser, page, size);
	}

	@PatchMapping("/seen")
	public NotificationAlertSeenResponse seen(@AuthenticatedUser CurrentUser currentUser) {
		return notificationService.seen(currentUser);
	}

	@PatchMapping("/read-all")
	public NotificationAlertReadAllResponse readAll(@AuthenticatedUser CurrentUser currentUser) {
		return notificationService.readAll(currentUser);
	}
}
