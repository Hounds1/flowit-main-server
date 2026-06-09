package dev.runtime_lab.flowit.domain.workspace.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;

public class DuplicateWorkspaceMemberException extends WorkspaceException {

	public DuplicateWorkspaceMemberException() {
		super(ErrorCode.WORKSPACE_JOIN_REQUEST_409_001, "이미 가입된 워크스페이스입니다.");
	}
}
