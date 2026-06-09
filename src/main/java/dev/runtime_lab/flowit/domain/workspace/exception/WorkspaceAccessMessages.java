package dev.runtime_lab.flowit.domain.workspace.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkspaceAccessMessages {

	public static final String MEMBERSHIP_REQUIRED = "반드시 워크스페이스의 멤버여야 합니다.";
	public static final String OWNER_REQUIRED = "워크스페이스는 반드시 한 명 이상의 소유자가 있어야 합니다.";
	public static final String ROLE_UPDATE_NOT_ALLOWED = "허용되지 않은 권한 변경입니다.";
	public static final String JOIN_REQUEST_HISTORY_ACCESS_DENIED =
		"허용되지 않은 가입 기록 열람입니다.";
	public static final String WORKSPACE_UPDATE_NOT_ALLOWED = "허용되지 않은 워크스페이스 업데이트입니다.";
}
