package dev.runtime_lab.flowit.domain.workspace.controller;

import dev.runtime_lab.flowit.domain.workspace.service.WorkspaceMemberService;
import dev.runtime_lab.flowit.global.security.authentication.AuthenticatedUser;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import dev.runtime_lab.flowit.global.web.response.ApiEmptyData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
public class WorkspaceMemberController {

	private final WorkspaceMemberService workspaceMemberService;

	@DeleteMapping("/{userId}")
	public ResponseEntity<ApiEmptyData> remove(
		@AuthenticatedUser CurrentUser currentUser,
		@PathVariable Long workspaceId,
		@PathVariable Long userId
	) {
		workspaceMemberService.remove(currentUser, workspaceId, userId);

		return ResponseEntity.ok(ApiEmptyData.empty());
	}
}
