package dev.runtime_lab.flowit.domain.user.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;

public class InvalidCurrentPasswordException extends UserException {

	public InvalidCurrentPasswordException() {
		super(ErrorCode.AUTH_401_001, "비밀번호가 일치하지 않습니다.");
	}
}
