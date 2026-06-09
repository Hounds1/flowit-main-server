package dev.runtime_lab.flowit.domain.auth.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;

public class InvalidRefreshTokenException extends AuthException {

	public InvalidRefreshTokenException() {
		super(ErrorCode.AUTH_401_001, "잘못된 재인증 토큰입니다.");
	}
}
