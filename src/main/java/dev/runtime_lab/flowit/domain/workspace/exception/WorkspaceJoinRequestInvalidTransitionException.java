package dev.runtime_lab.flowit.domain.workspace.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;

public class WorkspaceJoinRequestInvalidTransitionException extends WorkspaceException {

	public WorkspaceJoinRequestInvalidTransitionException() {
		super(ErrorCode.WORKSPACE_JOIN_REQUEST_409_002, "허가되지 않은 가입 요청입니다.");
	}
}
