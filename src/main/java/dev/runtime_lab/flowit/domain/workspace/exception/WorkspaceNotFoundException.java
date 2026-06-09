package dev.runtime_lab.flowit.domain.workspace.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;

public class WorkspaceNotFoundException extends WorkspaceException {

	public WorkspaceNotFoundException() {
		super(ErrorCode.WORKSPACE_404_001, "워크스페이스를 찾을 수 없습니다.");
	}
}
