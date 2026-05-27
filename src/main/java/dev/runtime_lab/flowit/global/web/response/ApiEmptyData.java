package dev.runtime_lab.flowit.global.web.response;

import java.util.LinkedHashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiEmptyData extends LinkedHashMap<String, Object> {

	public static ApiEmptyData empty() {
		return new ApiEmptyData();
	}
}
