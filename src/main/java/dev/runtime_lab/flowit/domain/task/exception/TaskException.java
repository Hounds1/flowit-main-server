package dev.runtime_lab.flowit.domain.task.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;
import dev.runtime_lab.flowit.global.web.exception.FlowitException;

public abstract class TaskException extends FlowitException {

	protected TaskException(ErrorCode errorCode) {
		super(errorCode);
	}

	protected TaskException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
