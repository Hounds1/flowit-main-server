package dev.runtime_lab.flowit.global.web.response;

import dev.runtime_lab.flowit.global.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiError {

	private final String code;
	private final String message;

	public static ApiError of(String code, String message) {
		return new ApiError(code, message);
	}

	public static ApiError from(ErrorCode errorCode) {
		return new ApiError(errorCode.getCode(), errorCode.getMessage());
	}

	public static ApiError from(ErrorCode errorCode, String message) {
		return new ApiError(errorCode.getCode(), message);
	}
}
