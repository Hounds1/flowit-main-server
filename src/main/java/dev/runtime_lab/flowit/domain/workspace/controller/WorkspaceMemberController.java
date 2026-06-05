package dev.runtime_lab.flowit.domain.workspace.controller;

import dev.runtime_lab.flowit.domain.workspace.dto.WorkspaceMemberRoleUpdateRequest;
import dev.runtime_lab.flowit.domain.workspace.dto.WorkspaceMembersResponse;
import dev.runtime_lab.flowit.domain.workspace.service.WorkspaceMemberService;
import dev.runtime_lab.flowit.global.security.authentication.AuthenticatedUser;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import dev.runtime_lab.flowit.global.web.response.ApiEmptyData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
public class WorkspaceMemberController {

	private final WorkspaceMemberService workspaceMemberService;

	@GetMapping
	public WorkspaceMembersResponse members(
		@AuthenticatedUser CurrentUser currentUser,
		@PathVariable Long workspaceId
	) {
		return workspaceMemberService.members(currentUser, workspaceId);
	}

	@PatchMapping("/{memberId}/role")
	public ApiEmptyData updateRole(
		@AuthenticatedUser CurrentUser currentUser,
		@PathVariable Long workspaceId,
		@PathVariable Long memberId,
		@Valid @RequestBody WorkspaceMemberRoleUpdateRequest request
	) {
		workspaceMemberService.updateRole(currentUser, workspaceId, memberId, request);

		return ApiEmptyData.empty();
	}

	@DeleteMapping("/{memberId}")
	public ApiEmptyData remove(
		@AuthenticatedUser CurrentUser currentUser,
		@PathVariable Long workspaceId,
		@PathVariable Long memberId
	) {
		workspaceMemberService.remove(currentUser, workspaceId, memberId);

		return ApiEmptyData.empty();
	}

	@DeleteMapping("/withdraw")
	public ApiEmptyData withdraw(
		@AuthenticatedUser CurrentUser currentUser,
		@PathVariable Long workspaceId
	) {
		workspaceMemberService.withdraw(currentUser, workspaceId);

		return ApiEmptyData.empty();
	}
}
