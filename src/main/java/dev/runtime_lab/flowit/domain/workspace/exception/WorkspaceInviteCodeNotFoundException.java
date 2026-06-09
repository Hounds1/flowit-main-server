package dev.runtime_lab.flowit.domain.workspace.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;

public class WorkspaceInviteCodeNotFoundException extends WorkspaceException {

	public WorkspaceInviteCodeNotFoundException() {
		super(ErrorCode.WORKSPACE_404_001, "초대 코드의 워크스페이스가 존재하지 않습니다.");
	}
}
