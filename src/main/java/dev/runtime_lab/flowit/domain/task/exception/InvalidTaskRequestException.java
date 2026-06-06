package dev.runtime_lab.flowit.domain.task.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;

public class InvalidTaskRequestException extends TaskException {

	public InvalidTaskRequestException(String message) {
		super(ErrorCode.VALIDATION_400_001, message);
	}
}
