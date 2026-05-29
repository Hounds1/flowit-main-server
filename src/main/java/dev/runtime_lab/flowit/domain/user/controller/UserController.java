package dev.runtime_lab.flowit.domain.user.controller;

import dev.runtime_lab.flowit.domain.user.dto.UserMeResponse;
import dev.runtime_lab.flowit.domain.user.service.UserMeService;
import dev.runtime_lab.flowit.global.security.authentication.AuthenticatedUser;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserMeService userMeService;

	@GetMapping("/me")
	public UserMeResponse me(@AuthenticatedUser CurrentUser currentUser) {
		return userMeService.getMe(currentUser);
	}
}
