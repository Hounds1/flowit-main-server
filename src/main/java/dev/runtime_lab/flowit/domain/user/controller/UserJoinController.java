package dev.runtime_lab.flowit.domain.user.controller;

import dev.runtime_lab.flowit.domain.user.dto.JoinRequest;
import dev.runtime_lab.flowit.domain.user.dto.JoinResponse;
import dev.runtime_lab.flowit.domain.user.service.UserJoinService;
import dev.runtime_lab.flowit.global.web.response.ApiCreatedData;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/public/users")
@RequiredArgsConstructor
public class UserJoinController {

	private final UserJoinService userJoinService;

	@PostMapping("/join")
	public ResponseEntity<ApiCreatedData> join(@Valid @RequestBody JoinRequest request) {
		JoinResponse response = userJoinService.join(request);

		return ResponseEntity
			.created(URI.create("/v1/public/users/%d".formatted(response.id())))
			.body(ApiCreatedData.afterCreated(response.id()));
	}
}
