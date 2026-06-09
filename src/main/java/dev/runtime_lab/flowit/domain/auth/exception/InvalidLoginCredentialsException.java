package dev.runtime_lab.flowit.domain.auth.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;

public class InvalidLoginCredentialsException extends AuthException {

	public InvalidLoginCredentialsException() {
		super(ErrorCode.AUTH_401_001, "존재하지 않는 이메일 또는 맞지 않는 비밀번호 입니다.");
	}
}
