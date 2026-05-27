package dev.runtime_lab.flowit.global.web.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiCreatedData {

	private final Long createdId;

	public static ApiCreatedData afterCreated(Long createdId) {
		return new ApiCreatedData(createdId);
	}
}
