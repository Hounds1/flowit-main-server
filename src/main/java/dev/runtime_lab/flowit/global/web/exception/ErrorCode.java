package dev.runtime_lab.flowit.global.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	VALIDATION_400_001(
		HttpStatus.BAD_REQUEST,
		"요청 값이 올바르지 않습니다.",
		"요청 본문 또는 파라미터 검증에 실패했습니다."
	),
	USER_409_001(
		HttpStatus.CONFLICT,
		"이미 가입된 이메일입니다.",
		"활성 사용자 중 동일한 이메일이 이미 존재합니다."
	);

	private final HttpStatus httpStatus;
	private final String message;
	private final String description;

	public String getCode() {
		return name();
	}
}
