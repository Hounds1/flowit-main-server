package dev.runtime_lab.flowit.domain.workspace.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;

public class WorkspaceInviteCodeGenerationException extends WorkspaceException {

	public WorkspaceInviteCodeGenerationException() {
		super(ErrorCode.WORKSPACE_500_001, "고유한 초대 코드를 생성하는데 실패했습니다.");
	}
}
