package dev.runtime_lab.flowit.domain.task.exception;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;

public class TaskNotFoundException extends TaskException {

	public TaskNotFoundException() {
		super(ErrorCode.TASK_404_001, "존재하지 않는 작업입니다.");
	}
}
