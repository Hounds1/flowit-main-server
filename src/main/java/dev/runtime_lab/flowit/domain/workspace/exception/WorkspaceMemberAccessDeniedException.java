package dev.runtime_lab.flowit.domain.workspace.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;

public class WorkspaceMemberAccessDeniedException extends WorkspaceException {

	public WorkspaceMemberAccessDeniedException() {
		super(ErrorCode.AUTH_403_001, "허가되지 않은 멤버 강제 퇴장입니다.");
	}

	public WorkspaceMemberAccessDeniedException(String message) {
		super(ErrorCode.AUTH_403_001, message);
	}
}
