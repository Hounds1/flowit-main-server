package dev.runtime_lab.flowit.domain.workspace.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;

public class InvalidWorkspaceUpdateException extends WorkspaceException {

	public InvalidWorkspaceUpdateException() {
		super(ErrorCode.WORKSPACE_400_001, "허용되지 않은 워크스페이스 업데이트입니다.");
	}
}
